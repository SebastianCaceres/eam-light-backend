package ch.cern.cmms.eamlightweb.workorders.activity;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.tools.InforException;
import ch.cern.eam.wshub.core.services.workorders.entities.LaborBooking;

@RestController
@RequestMapping("/bookinglabour")
public class BookingLabourRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping("/{workorder}")
	public ResponseEntity<?> readBookingLabours(@PathVariable("workorder") String workorder) {
		try {
			List<LaborBooking> labors = inforClient.getLaborBookingService().readLaborBookings(authenticationTools.getR5InforContext(), workorder);
			Collections.sort(labors);
			return ok(labors);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
