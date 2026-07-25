package ch.cern.cmms.eamlightweb.tools;

import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.entities.Entity;
import ch.cern.eam.wshub.core.services.entities.Pair;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult;
import ch.cern.eam.wshub.core.tools.GridTools;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EAMLightController {


    @Autowired
    protected AuthenticationTools authenticationTools;
    @Autowired
    protected InforClient inforClient;

    /**
     * Return an object included within the standard WS Hub response, with a OK (200) HTTP status
     * @param data
     * @param <T>
     * @return
     */
    public <T> ResponseEntity<?> ok(T data) {
        return ResponseEntity.ok(EAMResponse.fromData(data));
    }

    /**
     * Return an object included within the standard WS Hub response, with a NO_CONTENT (204) HTTP status
     * @param <T>
     * @return
     */
    public <T> ResponseEntity<?> noConent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Return an object included within the standard WS Hub response, with a SERVER_ERROR HTTP status
     * @param exception
     * @return
     */
    public ResponseEntity<?> serverError(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EAMResponse.fromException(exception));
    }

    /**
     * Return an object included within the standard WS Hub response, with a BAD_REQUEST HTTP status
     * @param exception
     * @return
     */
    public ResponseEntity<?> badRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EAMResponse.fromException(exception));
    }

    public ResponseEntity<?> forbidden(Exception exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(EAMResponse.fromException(exception));
    }

    public ResponseEntity<?> getPairListResponse(GridRequest gridRequest, String codeKey, String descKey) {
        try {
            return ok(inforClient.getTools().getGridTools().convertGridResultToObject(Pair.class,
                    Pair.generateGridPairMap(codeKey, descKey),
                    inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest)));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }

    public ResponseEntity<?> getEntityListResponse(GridRequest gridRequest, String codeKey, String descKey, String organizationKey) {
        try {
            return ok(inforClient.getTools().getGridTools().convertGridResultToObject(Entity.class,
                    Entity.generateGridEntityMap(codeKey, descKey, organizationKey),
                    inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest)));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }

    public ResponseEntity<?> getMapListResponse(GridRequest gridRequest) {
       return getMapListResponse(gridRequest, null, null);
    }

    public ResponseEntity<?> getMapListResponse(GridRequest gridRequest, String codeKey, String descKey) {
        try {
            final GridRequestResult gridRequestResult = inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest);
            final List<Map<String, String>> maps = GridTools.convertGridResultToMapList(gridRequestResult);
            final List<LinkedHashMap<String, String>> collect = maps.stream().map(s -> {
                    LinkedHashMap<String, String> cloneMap = new LinkedHashMap<>();
                    if (codeKey != null) cloneMap.put("code", s.get(codeKey));
                    if (descKey != null) cloneMap.put("desc", s.get(descKey));
                    cloneMap.putAll(s);
                    return cloneMap;
                }).collect(Collectors.toList());
            return ok(collect);
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }
}
