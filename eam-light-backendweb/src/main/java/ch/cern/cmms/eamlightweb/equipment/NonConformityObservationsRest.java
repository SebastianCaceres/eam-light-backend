package ch.cern.cmms.eamlightweb.equipment;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.equipment.entities.NonConformityObservation;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ncrobservations")
public class NonConformityObservationsRest extends EAMLightController {

    @Autowired
    private InforClient inforClient;
    @Autowired
    private AuthenticationTools authenticationTools;

    @GetMapping("/{ncr}")
    public ResponseEntity<?> loadNonConformityObservations(@PathVariable("ncr") String ncr) {
        try {
            List<Map<String, String>> additionalCostsList = new ArrayList<>();
            if (ncr != null) {
                String organization = authenticationTools.getR5InforContext().getOrganizationCode();

                GridRequest gridRequest = new GridRequest("OSNCHD_OBS");
                gridRequest.setUserFunctionName("OSNCHD");
                gridRequest.addParam("param.nonconformity", ncr);
                gridRequest.addParam("param.organization", organization);
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

    @PostMapping
    public ResponseEntity<?> createNonConformityObservation(@RequestBody NonConformityObservation nonConformityObservation) {
        try {
            return ok(inforClient.getNonConformityObservationService().createNonConformityObservation(authenticationTools.getInforContext(), nonConformityObservation));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }
}
