package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.workorders.entities.Activity;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivitiesRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;
	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.ActivityRepository activityRepository;

	@GetMapping("/read")
	public ResponseEntity<?> readActivities(@RequestParam("workorder") String number, @RequestParam(value = "includeChecklists", defaultValue = "true") Boolean includeChecklists) {
		if (activityRepository != null) {
			try {
				List<Activity> activities = activityRepository.findByWorkOrder(number);
				if (activities != null) {
					return ok(activities);
				}
			} catch (Exception ignored) {}
		}
		return ok(new java.util.ArrayList<>());
	}

	@PostMapping
	public ResponseEntity<?> createActivity(@RequestBody Activity activity) {
		try {
			if (activityRepository != null) {
				return ok(activityRepository.save(activity));
			}
			return ok(activity);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	
	
	public ResponseEntity<?> updateActivity(Activity activity) {
		try {
			return ok(inforClient.getLaborBookingService().updateActivity(authenticationTools.getInforContext(), activity, "confirmed"));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/init/{workorder}")
	
	
	public ResponseEntity<?> initActivity(@PathVariable("workorder") String number) {
		try {
			Activity activity = new Activity();
			return ok(activity);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@DeleteMapping
	
	public ResponseEntity<?> deleteActivity(Activity activity) {
		try {
			return ok(inforClient.getLaborBookingService().deleteActivity(authenticationTools.getInforContext(), activity));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}
}
