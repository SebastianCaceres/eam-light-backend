package ch.cern.cmms.eamlightweb.workorders.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestFilter;
import ch.cern.eam.wshub.core.tools.InforException;

@RestController
@RequestMapping("/workordersmisc")
public class WorkOrderMisc extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping("/equipment")
	public ResponseEntity<?> getWOEquipLinearDetails(@RequestParam("eqCode") String eqCode) {
		return ok(new HashMap<>());
	}

	@GetMapping("/childrenwo/{workorder:.+}")
	public ResponseEntity<?> getChildrenWorkOrders(@PathVariable("workorder") String workorder) {
		return ok(new java.util.ArrayList<>());
	}

	@GetMapping("/eqpmecwo/{workorder:.+}")
	public ResponseEntity<?> getWorkOrderEquipment(@PathVariable("workorder") String workorder) {
		return ok(new java.util.ArrayList<>());
	}

	@GetMapping("/otherid/{workorder:.+}")
	public ResponseEntity<?> getWOEqOtherIds(@PathVariable("workorder") String workorder) {
		try {
			GridRequest gridRequestWoEqOi = new GridRequest("UUOIEQ", 1000);
			gridRequestWoEqOi.addFilter("workorder", workorder, "EQUALS", GridRequestFilter.JOINER.AND);
			Map<String, String> eqToOtherId = inforClient.getTools().getGridTools().convertGridResultToMap("equipment", "otherid",
									inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequestWoEqOi));
			return ok(eqToOtherId);
		} catch(Exception e) {
			return ok(new HashMap<>());
		}
	}
}