package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.repositories.StandardWorkOrderRepository;
import ch.cern.eam.wshub.core.services.workorders.entities.StandardWorkOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/stdworkorders")
public class StandardWorkOrderRest extends EAMLightController {

    @Autowired(required = false)
    private StandardWorkOrderRepository standardWorkOrderRepository;

    @GetMapping("/{stdworkorder}")
    public ResponseEntity<?> readWorkOrder(@PathVariable("stdworkorder") String number) {
        if (standardWorkOrderRepository != null && number != null) {
            try {
                Optional<StandardWorkOrder> opt = standardWorkOrderRepository.findById(number);
                if (opt.isPresent()) {
                    return ok(opt.get());
                }
            } catch (Exception ignored) {}
        }
        StandardWorkOrder stdWo = new StandardWorkOrder();
        stdWo.setCode(number);
        stdWo.setDesc("Standard Work Order " + number);
        return ok(stdWo);
    }
}
