package ch.cern.cmms.eamlightweb.equipment;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.entities.UserDefinedFields;
import ch.cern.eam.wshub.core.services.equipment.entities.Equipment;
import ch.cern.eam.wshub.core.services.equipment.entities.NonConformity;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ncrs")

public class NonConformitiesRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.NonConformityRepository nonConformityRepository;

	@GetMapping("/{code}")
	public ResponseEntity<?> readNonConformity(@PathVariable("code") String code) {
		if (nonConformityRepository != null) {
			try {
				java.util.Optional<NonConformity> opt = nonConformityRepository.findById(code);
				if (opt.isPresent()) {
					return ok(opt.get());
				}
			} catch (Exception ignored) {}
		}
		NonConformity ncr = new NonConformity();
		ncr.setCode(code);
		ncr.setDescription("NCR " + code);
		return ok(ncr);
	}

	@PostMapping
	public ResponseEntity<?> createNonConformity(@RequestBody NonConformity nonConformity) {
		try {
			if (nonConformityRepository != null) {
				return ok(nonConformityRepository.save(nonConformity));
			}
			return ok(nonConformity);
		} catch (Exception e) {
			return serverError(e);
		}
	}

	@PutMapping
	public ResponseEntity<?> updateNonConformity(NonConformity nonConformity) {
		try {
			return ok(inforClient.getNonconformityService().updateNonconformity(authenticationTools.getInforContext(), nonConformity));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@DeleteMapping("/{code}")
	public ResponseEntity<?> deleteNonConformity(@PathVariable("code") String code) {
		try {
			inforClient.getNonconformityService().deleteNonconformity(authenticationTools.getInforContext(), code);
			return noConent();
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}


	@GetMapping("/init")
	public ResponseEntity<?> initNonConformity() {
		try {
			java.util.Map<String, Object> udfMap = new java.util.HashMap<>();
			udfMap.put("UDFCHKBOX01", "+");
			udfMap.put("udfchkbox01", "+");

			java.util.Map<String, Object> cfMap = new java.util.HashMap<>();
			cfMap.put("CUSTOMFIELD", new java.util.ArrayList<>());
			cfMap.put("customFields", new java.util.ArrayList<>());

			java.util.Map<String, Object> defaultObj = new java.util.HashMap<>();
			defaultObj.put("statusCode", "U");
			defaultObj.put("status", "U");
			defaultObj.put("userDefinedFields", udfMap);
			defaultObj.put("customFields", new java.util.ArrayList<>());
			defaultObj.put("USERDEFINEDAREA", cfMap);
			defaultObj.put("fields", new java.util.HashMap<>());

			java.util.Map<String, Object> map = new java.util.HashMap<>();
			map.put("Nonconformity", defaultObj);
			map.put("NonconformityDefault", defaultObj);
			map.put("nonconformity", defaultObj);
			map.put("NCR", defaultObj);
			map.put("NCRDefault", defaultObj);
			map.put("ncr", defaultObj);
			return ok(map);
		} catch (Exception e) {
			return serverError(e);
		}
	}


	@GetMapping("/equipment/{asset}")
	public ResponseEntity<?> getEquipmentNonConformities(@PathVariable("asset") String asset) {
		try {
			List<Map<String, String>> assetNonConformities = new ArrayList<>();
			if (asset != null) {
				GridRequest gridRequest = new GridRequest("OSNCHD");
				gridRequest.setUserFunctionName("OSNCHD");
				gridRequest.addFilter("equipment", asset, "=");
				try {
					assetNonConformities = inforClient.getTools().getGridTools().convertGridResultToMapList(
							inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequest)
					);
				} catch (Exception ignored) {
					assetNonConformities = new ArrayList<>();
				}
			}
			return ok(assetNonConformities);
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
