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

	@GetMapping("/{code}")
	public ResponseEntity<?> readNonConformity(@PathVariable("code") String code) {
		try {
			return ok(inforClient.getNonconformityService().readNonconformity(authenticationTools.getInforContext(), code));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
	public ResponseEntity<?> createNonConformity(NonConformity nonConformity) {
		try {
			return ok(inforClient.getNonconformityService().createNonconformity(authenticationTools.getInforContext(), nonConformity));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
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
				NonConformity nonConformity = inforClient.getNonconformityService().readNonconformityDefault(authenticationTools.getInforContext(), "");
				nonConformity.setCustomFields(inforClient.getTools().getCustomFieldsTools().getWSHubCustomFields(authenticationTools.getInforContext(), "NOCF", "*"));
				nonConformity.setUserDefinedFields(new UserDefinedFields());
				return ok(nonConformity);
			} catch (InforException e) {
				return badRequest(e);
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
				assetNonConformities = inforClient.getTools().getGridTools().convertGridResultToMapList(
						inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequest)
				);
			}
			return ok(assetNonConformities);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
