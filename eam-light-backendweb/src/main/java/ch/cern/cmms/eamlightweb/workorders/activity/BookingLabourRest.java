package ch.cern.cmms.eamlightweb.workorders.activity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.tools.InforException;
import ch.cern.eam.wshub.core.services.workorders.entities.LaborBooking;

@RestController
@RequestMapping("/bookinglabour")
public class BookingLabourRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;
	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.LaborBookingRepository laborBookingRepository;

	@GetMapping
	@RequestMapping("/{workorder}")
	public ResponseEntity<?> readBookingLabours(@PathVariable("workorder") String workorder) {
		if (laborBookingRepository != null) {
			try {
				List<LaborBooking> labors = laborBookingRepository.findByWorkOrder(workorder);
				if (labors != null) {
					Collections.sort(labors);
					return ok(labors);
				}
			} catch (Exception ignored) {}
		}
		return ok(new java.util.ArrayList<>());
	}

}
