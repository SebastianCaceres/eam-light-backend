package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/stdworkorders")
public class StandardWorkOrderRest extends EAMLightController {

    @GetMapping("/{stdworkorder}")
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
