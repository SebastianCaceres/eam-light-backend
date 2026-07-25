package ch.cern.cmms.eamlightweb.tools.autocomplete;

import ch.cern.cmms.eamlightejb.equipment.EquipmentEJB;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Arrays;

@RestController
@RequestMapping("/autocomplete")

public class AutocompleteEquipment extends EAMLightController {

	@Autowired
	private EquipmentEJB equipmentEJB;

	@GetMapping
	@RequestMapping("/eqp")
	
	
	public ResponseEntity<?> complete(@RequestParam("s") String code, @RequestParam("filterL") Boolean excludeLocations) {
		try {
			return ok(equipmentEJB.getEquipmentSearchResults(code, excludeLocations ? Arrays.asList("A", "P", "S") : null, authenticationTools.getInforContext()));
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
