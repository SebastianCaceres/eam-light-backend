package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.workorders.entities.Activity;
import ch.cern.eam.wshub.core.services.workorders.entities.TaskPlan;
import ch.cern.eam.wshub.core.services.workorders.entities.WorkOrderActivityChecklistItem;
import ch.cern.eam.wshub.core.services.workorders.entities.WorkOrderActivityChecklistSignature;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.math.BigInteger;

@RequestMapping("/checklists")

public class ChecklistController extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@PutMapping
	
	
	public ResponseEntity<?> updateChecklist(
			WorkOrderActivityChecklistItem checklistItem,
			@RequestParam("taskPlanCode") String taskPlanCode
	) {
		try {
			TaskPlan taskPlan = null;
			if (taskPlanCode != null && !taskPlanCode.isEmpty()) {
				taskPlan = new TaskPlan();
				taskPlan.setCode(taskPlanCode);
				taskPlan.setTaskRevision(BigInteger.ZERO);
			}
			return ok(inforClient.getChecklistService().updateWorkOrderChecklistItem(
					authenticationTools.getInforContext(),
					checklistItem,
					taskPlan
			));
		} catch (InforException e) {
			return badRequest(e);
		} catch (Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
	@RequestMapping("/workorders")
	
	
	public ResponseEntity<?> createFollowUpWorkOrders(Activity activity) {
		try {
			return ok(inforClient.getChecklistService().createFollowUpWorkOrders(authenticationTools.getInforContext(), activity));
		} catch (InforException e) {
			return badRequest(e);
		} catch (Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	@RequestMapping("/esign")
	
	
	public ResponseEntity<?> eSignWorkOrderActivityChecklist(WorkOrderActivityChecklistSignature workOrderActivityCheckListSignature) {
		try {
			return ok(inforClient.getChecklistService().eSignWorkOrderActivityChecklist(authenticationTools.getInforContext(), workOrderActivityCheckListSignature));
		} catch (InforException e) {
			return badRequest(e);
		} catch (Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/definition/{taskplanid}/{id}")
	
	public ResponseEntity<?> getChecklistDefinition(@PathVariable("taskplanid") String taskPlanCode, @PathVariable("id") String id) {
		try {
			TaskPlan taskPlan = new TaskPlan();
			taskPlan.setCode(taskPlanCode);
			taskPlan.setTaskRevision(BigInteger.ZERO);
			return ok(inforClient.getChecklistService().getChecklistDefinition(authenticationTools.getInforContext(), taskPlan, id));
		} catch (InforException e) {
			return badRequest(e);
		} catch (Exception e) {
			return serverError(e);
		}
	}
}
