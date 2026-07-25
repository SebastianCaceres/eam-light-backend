package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/stdworkorders")

public class StandardWorkOrderRest extends EAMLightController {

    @GetMapping
    @RequestMapping("/{stdworkorder}")
    
    
    public ResponseEntity<?> readWorkOrder(@PathVariable("stdworkorder") String number) {
        try {
            return ok(inforClient.getStandardWorkOrderService().readStandardWorkOrder(authenticationTools.getInforContext(), number));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }

}
