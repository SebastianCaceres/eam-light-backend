package ch.cern.cmms.eamlightweb.equipment.autocomplete;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;

@RestController
@RequestMapping("/autocomplete")

public class AutocompleteEquipmentParent extends EAMLightController {

	@Autowired
	private AuthenticationTools authenticationTools;
	@Autowired
	private InforClient inforClient;

	@GetMapping
	@RequestMapping("/eqp/parent/{type}")
	
	
	public ResponseEntity<?> complete(@PathVariable("type") String type, @RequestParam("code") String code) {
			GridRequest gridRequest = new GridRequest( "LVOBJL_EQ", GridRequest.GRIDTYPE.LIST, ApplicationData.AUTOCOMPLETE_RESULT_SIZE);
			gridRequest.setUseNative(false);

			gridRequest.addParam("param.objectrtype", type);
			gridRequest.addParam("param.bypassdeptsecurity", null);
			gridRequest.addParam("param.objectcode", "");
			gridRequest.addParam("param.objectorg", authenticationTools.getOrganizationCode());
			gridRequest.addParam("control.org", authenticationTools.getOrganizationCode());

			gridRequest.addFilter("equipmentcode", code.toUpperCase(), "BEGINS");
			gridRequest.sortBy("equipmentcode");
			return getEntityListResponse(gridRequest, "equipmentcode", "description_obj", "equiporganization");
	}

}