package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightejb.workorders.WorkOrderEJB;
import ch.cern.cmms.eamlightejb.workorders.entity.WorkOrderEntity;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proxy/workorders")
public class WorkOrderRest extends EAMLightController {

    @Autowired
    private WorkOrderEJB workOrderEJB;

    @GetMapping("/{number}")
    public ResponseEntity<?> readWorkOrder(@PathVariable("number") String number) {
        return workOrderEJB.readWorkOrder(number)
                .map(this::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createWorkOrder(@RequestBody WorkOrderEntity wo) {
        try {
            return ok(workOrderEJB.createWorkOrder(wo));
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PutMapping("/{number}")
    public ResponseEntity<?> updateWorkOrder(@PathVariable("number") String number, @RequestBody WorkOrderEntity wo) {
        try {
            wo.setCode(number);
            return ok(workOrderEJB.updateWorkOrder(wo));
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<?> deleteWorkOrder(@PathVariable("number") String number) {
        try {
            workOrderEJB.deleteWorkOrder(number);
            return ok(number);
        } catch (Exception e) {
            return serverError(e);
        }
    }
}
