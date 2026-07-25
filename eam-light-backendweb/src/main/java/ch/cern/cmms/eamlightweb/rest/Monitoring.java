package ch.cern.cmms.eamlightweb.rest;

import ch.cern.cmms.eamlightejb.MonitoringService.MonitoringService;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.eam.wshub.core.tools.InforException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/monitoring")
public class Monitoring {

    @Autowired
    private AuthenticationTools authenticationTools;

    @Autowired
    private MonitoringService monitoringService;

    @GetMapping
    public ResponseEntity<?> monitor(@RequestParam("equipment") String equipment, @RequestParam("workorder") String number) {
        Map<String, String> responses = new HashMap<>();
        boolean check = true;
        try {
            responses = monitoringService.monitoring(equipment, number, authenticationTools.getInforContext());
        } catch (InforException e) {
            e.printStackTrace();
        }
        String str = "ERROR ";

        for (String key : responses.values()) {
            if (key.contains(str)) {
                check = false;
            }
        }
        if (!check) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responses);
        } else {
            return ResponseEntity.ok(responses);
        }
    }

}
