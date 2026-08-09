package ch.cern.cmms.eamlightweb.equipment;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.equipment.entities.NonConformityObservation;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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

    @Autowired(required = false)
    private ch.cern.eam.wshub.core.repositories.NCRObservationRepository ncrObservationRepository;

    @GetMapping("/{ncr}")
    public ResponseEntity<?> loadNonConformityObservations(@PathVariable("ncr") String ncr) {
        try {
            List<Map<String, String>> observationsList = new ArrayList<>();
            if (ncr != null && ncrObservationRepository != null) {
                try {
                    List<ch.cern.eam.wshub.core.services.equipment.entities.NCRObservation> entities = ncrObservationRepository.findByNonConformityCode(ncr);
                    if (entities != null) {
                        observationsList = entities.stream().map(ent -> {
                            Map<String, String> m = new java.util.HashMap<>();
                            m.put("nonconformity", ent.getNonConformityCode());
                            m.put("observation", ent.getCode());
                            return m;
                        }).collect(java.util.stream.Collectors.toList());
                    }
                } catch (Exception ignored) {}
            }
            return ok(observationsList);
        } catch(Exception e) {
            return ok(new ArrayList<>());
        }
    }

    @PostMapping
    public ResponseEntity<?> createNonConformityObservation(NonConformityObservation nonConformityObservation) {
        try {
            return ok(inforClient.getNonConformityObservationService().createNonConformityObservation(authenticationTools.getInforContext(), nonConformityObservation));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }
}
