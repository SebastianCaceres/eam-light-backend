package ch.cern.cmms.eamlightweb.equipment;

import ch.cern.cmms.eamlightejb.equipment.EquipmentEJB;
import ch.cern.cmms.eamlightweb.codegenerator.CodeGeneratorService;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.cmms.standardworkorders.MTFWorkOrderServiceImpl;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.equipment.entities.EquipmentReplacement;
import ch.cern.eam.wshub.core.services.material.entities.PartAssociation;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/equipment")

public class EquipmentRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private EquipmentEJB equipmentEJB;
	@Autowired
	private EquipmentReplacementService equipmentReplacementService;
	@Autowired
	private AuthenticationTools authenticationTools;

	@Autowired
	private CodeGeneratorService codeGeneratorService;
	@Autowired(required = false)
	private MTFWorkOrderServiceImpl mtfStandardWorkOrderService;

	@PostMapping
	@RequestMapping("/replace")
	
	
	public ResponseEntity<?> replaceEquipment(EquipmentReplacement eqpReplacement) {
		try {
			return ok(equipmentReplacementService.replaceEquipment(authenticationTools.getInforContext(), eqpReplacement));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/collectdetachables/{oldEquipment}")
	
	public ResponseEntity<?> collectDetachableEquipment(@PathVariable("oldEquipment") String oldEquipmentCode) {
		try {
			return ok(equipmentReplacementService.collectDetachableEquipment(authenticationTools.getInforContext(), oldEquipmentCode));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/children/{equipment}")
	public ResponseEntity<?> getEquipmentChildren(@PathVariable("equipment") String equipment) {
		try {
			if (equipmentEJB == null) {
				return ok(new java.util.ArrayList<>());
			}
			java.util.List<?> result = equipmentEJB.getEquipmentChildren(equipment);
			if (result == null) {
				result = new java.util.ArrayList<>();
			}
			return ok(result);
		} catch(Exception e) {
			return ok(new java.util.ArrayList<>());
		}
	}

	@GetMapping
	@RequestMapping("/{eqCode}/mtfsteps/maxstep")
	
	public ResponseEntity<?> getEquipmentStandardWOMaxStep(@PathVariable("eqCode") String eqCode, @RequestParam("swo") String swo) {
		try {
			return ok(mtfStandardWorkOrderService.getEquipmentStandardWOMaxStep(eqCode, swo));
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
       @RequestMapping("/partsassociated")
       
       public ResponseEntity<?> createPartAssociation(PartAssociation partAssociation) {
	   try {
		   return ok(inforClient.getPartMiscService().createPartAssociation(authenticationTools.getInforContext(), partAssociation));
	   } catch(Exception e) {
		   return serverError(e);
	   }
	}

}
