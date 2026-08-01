package ch.cern.cmms.eamlightweb.application;

import ch.cern.cmms.eamlightejb.cache.CacheUtils;
import ch.cern.cmms.eamlightejb.cache.Cacheable;
import ch.cern.cmms.eamlightweb.cache.CacheManager;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult;
import ch.cern.eam.wshub.core.tools.GridTools;
import ch.cern.eam.wshub.core.tools.InforException;
import ch.cern.eam.wshub.core.tools.Tools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ApplicationService implements Cacheable {

    private static final String SERVICE_ACCOUNTS_PARAM = "EL_SERVI";

    private final ObjectMapper mapper = new ObjectMapper();
    private final LoadingCache<String, Map<String, String>> paramFieldCache = CacheUtils.buildLoadingCache(this::loadParams);

    @Autowired
    private InforClient inforClient;
    @Autowired
    private AuthenticationTools authenticationTools;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public void clearCache() {
        paramFieldCache.invalidateAll();
    }

    @Override
    public void setExpiresAfter(long l, TimeUnit timeUnit) {
        CacheUtils.updateCacheTimeout(paramFieldCache, l, timeUnit);
    }

    public Map<String, String> getParams(String tenant) throws InforException {
        InforContext context = new InforContext();
        context.setTenant(tenant);
        String paramFieldCacheKey = Tools.getCacheKey(context, "params");
        return paramFieldCache.get(paramFieldCacheKey);
    }

    private Map<String, String> loadParams(String key) {
        Map<String, String> params = new HashMap<>();
        try {
            InforContext r5Context = authenticationTools.getR5InforContext();
            GridRequest gridRequest = new GridRequest("BSINST");
            gridRequest.addFilter("installcode", "EL_", "BEGINS");

            GridRequestResult result = inforClient.getGridsService().executeQuery(r5Context, gridRequest);
            params = GridTools.convertGridResultToMap("installcode", "value", result);
        } catch (Exception e) {
            // Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
            params.put("EL_CACTO", "60");
            params.put("EL_EWOST", "R");
            params.put("EL_EVOEQ", "SYSTEM");
        }

        String timeoutStr = params.get("EL_CACTO");
        try {
            long timeout = Long.parseLong(timeoutStr);
            cacheManager.setAllExpiresAfter(timeout, TimeUnit.MINUTES);
        } catch (NumberFormatException | NullPointerException e) {
            System.err.println("Invalid or missing cache timeout value for EL_CACTO: " + timeoutStr);
        }

        return params;
    }

    public Map<String, String> getServiceAccounts(String tenant) throws InforException {
        try {
            Map<String, String> params = getParams(tenant);
            if (!params.containsKey(SERVICE_ACCOUNTS_PARAM)) {
                return new HashMap<>();
            }
            String serviceAccountsParam = params.get(SERVICE_ACCOUNTS_PARAM);
            return mapper.readValue(serviceAccountsParam, Map.class);
        } catch (InforException | JsonProcessingException e) {
            e.printStackTrace();
            throw new InforException("Could not read allowed service accounts", null, null);
        }
    }
}