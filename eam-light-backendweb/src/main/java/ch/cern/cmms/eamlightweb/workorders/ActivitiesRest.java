package ch.cern.cmms.eamlightweb.workorders;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.workorders.entities.Activity;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/activities")
public class ActivitiesRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping("/read")
	public ResponseEntity<?> readActivities(@RequestParam("workorder") String number, @RequestParam(value = "includeChecklists", defaultValue = "true") Boolean includeChecklists) {
		try {
			return ok(inforClient.getLaborBookingService().readActivities(authenticationTools.getInforContext(), number, includeChecklists));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
	public ResponseEntity<?> createActivity(@RequestBody Activity activity) {
		try {
			return ok(inforClient.getLaborBookingService().createActivity(authenticationTools.getInforContext(),activity));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	public ResponseEntity<?> updateActivity(@RequestBody Activity activity) {
		try {
			return ok(inforClient.getLaborBookingService().updateActivity(authenticationTools.getInforContext(), activity, "confirmed"));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping("/init/{workorder}")
	public ResponseEntity<?> initActivity(@PathVariable("workorder") String number) {
		try {
			Activity activity = new Activity();
			return ok(activity);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@DeleteMapping
	public ResponseEntity<?> deleteActivity(@RequestBody Activity activity) {
		try {
			return ok(inforClient.getLaborBookingService().deleteActivity(authenticationTools.getInforContext(), activity));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}
}
