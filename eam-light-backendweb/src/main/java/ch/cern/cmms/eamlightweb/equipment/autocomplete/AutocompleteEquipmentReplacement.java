package ch.cern.cmms.eamlightweb.equipment.autocomplete;

import ch.cern.cmms.eamlightejb.equipment.EquipmentEJB;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;

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

public class AutocompleteEquipmentReplacement extends EAMLightController {

	@Autowired
	private EquipmentEJB equipmentEJB;

	@GetMapping
	@RequestMapping("/eqp/eqpreplace/{code}")
	
	
	public ResponseEntity<?> complete(@PathVariable("code") String code, @RequestParam("filterL") Boolean excludeLocations) {
		try {
			return ok(equipmentEJB.getEquipmentSearchResults(code, excludeLocations ? Arrays.asList("A", "P", "S") : null, authenticationTools.getInforContext()));
		} catch(Exception e) {
			return serverError(e);
		}
	}

}