package ch.cern.cmms.eamlightweb.workorders.additionalcosts;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workorders")
public class AdditionalCostsRest extends EAMLightController {

    @Autowired
    private InforClient inforClient;
    @Autowired
    private AuthenticationTools authenticationTools;

    @GetMapping("/{workorder}/additionalcosts")
    public ResponseEntity<?> loadAdditionalCostsList(@PathVariable("workorder") String workorder) {
        try {
            List<Map<String, String>> additionalCostsList = new ArrayList<>();
            if (workorder != null) {
                GridRequest gridRequest = new GridRequest("WSJOBS_ACO");
                gridRequest.setUserFunctionName("WSJOBS");
                gridRequest.addParam("param.workordernum", workorder);
                gridRequest.addParam("param.headeractivity", "0");
                gridRequest.addParam("param.headerjob", "0");
                gridRequest.sortBy("additionalcostsdate");

                additionalCostsList = inforClient.getTools().getGridTools().convertGridResultToMapList(
                        inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequest)
                );
            }
            return ok(additionalCostsList);
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }

}
