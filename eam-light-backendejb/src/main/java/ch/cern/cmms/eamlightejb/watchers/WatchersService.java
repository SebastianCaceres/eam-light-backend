package ch.cern.cmms.eamlightejb.watchers;

import ch.cern.eam.wshub.core.repositories.WatcherRepository;
import ch.cern.eam.wshub.core.services.workorders.entities.Watcher;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.administration.entities.EAMUser;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestFilter;
import ch.cern.eam.wshub.core.tools.GridTools;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WatchersService {

    @Autowired
    private InforClient inforClient;

    @Autowired(required = false)
    private WatcherRepository watcherRepository;

    public List<Map<String, String>> getAutocompleteOptions(InforContext r5Context, String code) throws InforException {
        GridRequest gridRequest = new GridRequest("BSUSER", GridRequest.GRIDTYPE.LIST, 10);

        String uppercasedCode = code.toUpperCase();

        gridRequest.addFilter("usercode", uppercasedCode, "BEGINS", GridRequestFilter.JOINER.OR);

        Arrays.stream(uppercasedCode.split(" ")).forEach(name -> {
            gridRequest.addFilter("description", " " + name, "CONTAINS",
                    GridRequestFilter.JOINER.OR, true, false);

            gridRequest.addFilter("description", name, "BEGINS",
                    GridRequestFilter.JOINER.AND, false, true);
        });

        gridRequest.sortBy("description");

        return GridTools.convertGridResultToMapList(inforClient.getGridsService()
                .executeQuery(r5Context, gridRequest));
    }

    public List<EAMUser> getWatchersForWorkOrder(InforContext context, String woCode) throws InforException {
        if (watcherRepository == null) {
            return new ArrayList<>();
        }
        List<Watcher> watchers = watcherRepository.findByWorkOrderCode(woCode);

        return watchers.stream().map((watcher) -> {
            String usercode = watcher.getPerson();
            try {
                return inforClient.getUserSetupService().readUserSetup(context, usercode);
            } catch (Exception ignored) {
                EAMUser unknownUser = new EAMUser();
                unknownUser.setUserCode(usercode);
                return unknownUser;
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    public String addWatchersToWorkOrder(InforContext context, InforContext r5Context, String woCode, List<String> userNames)
                                          throws InforException {
        List<WatcherInfo> filteredUserNames = getFilteredWatcherInfo(woCode, userNames);

        List<Watcher> entities = filteredUserNames.stream().map(watcher -> {
            Watcher entity = new Watcher();
            entity.setWorkOrderCode(woCode);
            entity.setPerson(watcher.getUserCode());
            return entity;
        }).collect(Collectors.toList());

        if (watcherRepository != null) {
            watcherRepository.saveAll(entities);
        }
        return "SUCCESS";
    }

    @Transactional
    public int removeWatchersFromWorkOrder(InforContext context, String woCode, List<String> userNames) {
        if (watcherRepository != null && userNames != null && !userNames.isEmpty()) {
            watcherRepository.deleteByWorkOrderCodeAndPersonIn(woCode, userNames);
            return userNames.size();
        }
        return 0;
    }

    public List<WatcherInfo> getFilteredWatcherInfo(String woCode, List<String> userCodes) {
        if (userCodes.isEmpty()) {
            return new ArrayList<>();
        }

        return inforClient.getTools().getEntityManager()
                .createNamedQuery(WatcherInfo.FILTER_WATCHERS_BY_WO_ACCESS_LIST, WatcherInfo.class)
                .setParameter("evtCode", woCode)
                .setParameter("usrList", userCodes)
                .getResultList();
    }

    public List<WatcherInfo> getFilteredWatcherInfo(String woCode, String hint) {
        if (hint == null) {
            return new ArrayList<>();
        }

        final String orderedNames = Arrays.stream(hint.split(" "))
                .map(String::toUpperCase)
                .map(String::trim)
                .sorted()
                .map(str -> str + ".*")
                .collect(Collectors.joining(","));
        final String regex = "(^|,)" + orderedNames;

        return inforClient.getTools().getEntityManager()
                .createNamedQuery(WatcherInfo.FILTER_WATCHERS_BY_WO_ACCESS_HINT, WatcherInfo.class)
                .setParameter("evtCode", woCode)
                .setParameter("hint", hint.trim().toUpperCase() + "%")
                .setParameter("regex", regex)
                .setMaxResults(30)
                .getResultList();
    }
}
