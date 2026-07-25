package ch.cern.cmms.eamlightweb.meter;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.tools.InforException;
import ch.cern.eam.wshub.core.services.workorders.entities.MeterReading;

@RequestMapping("/meters")

public class MeterRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@PostMapping
	
	
	public ResponseEntity<?> createReading(MeterReading meterReading) {
		try {
			return ok(inforClient.getWorkOrderMiscService().createMeterReading(authenticationTools.getInforContext(), meterReading));
		} catch (InforException e) {
			return badRequest(e);
		} catch (Exception e) {
			return serverError(e);
		}
	}
}

