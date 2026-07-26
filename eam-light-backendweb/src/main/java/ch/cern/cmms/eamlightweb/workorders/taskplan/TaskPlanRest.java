package ch.cern.cmms.eamlightweb.workorders.taskplan;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.workorders.entities.TaskPlan;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/taskplan")
public class TaskPlanRest extends EAMLightController {
    @Autowired
    private InforClient inforClient;
    @Autowired
    private AuthenticationTools authenticationTools;

    @GetMapping("/{taskcode}")
    public ResponseEntity<?> readTaskPlan (@PathVariable("taskcode") String taskCode) {
        try {
            TaskPlan taskPlan = new TaskPlan();
            taskPlan.setCode(taskCode);
            return ok(inforClient.getTaskPlanService().getTaskPlan(authenticationTools.getInforContext(), taskPlan));
        } catch (InforException e) {
            return badRequest(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

}
