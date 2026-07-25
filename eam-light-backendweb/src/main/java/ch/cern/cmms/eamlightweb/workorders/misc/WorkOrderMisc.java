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
import net.datastream.schemas.mp_results.mp7336_001.AdditionalWOEquipDetails;

@RequestMapping("/workordersmisc")

public class WorkOrderMisc extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@GetMapping
	@RequestMapping("/eqpmecwo/{workorder}")
	
	
	public ResponseEntity<?> getWorkOrderEquipment(@PathVariable("workorder") String workorder) {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("247", "equipmentCode");
			map.put("249", "equipmentDesc");
			map.put("1872", "equipmentType");
			map.put("448", "equipmentTypeDesc");

			GridRequest gridRequest = new GridRequest("1631", "WSJOBS_MEC", "1618");
			gridRequest.addParam("param.workordernum", workorder);

			List<WorkOrderEquipment> childrenWOs = inforClient.getTools().getGridTools().convertGridResultToObject(WorkOrderEquipment.class,
					map,
					inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequest));
			return ok(childrenWOs);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/childrenwo/{workorder}")
	
	
	public ResponseEntity<?> getChildrenWorkOrders(@PathVariable("workorder") String workorder) throws InforException {
		try {
			Map<String, String> map = new HashMap<>();
			map.put("1", "number");
			map.put("2", "description");
			map.put("5", "equipment");
			map.put("16", "status");
			map.put("12", "type");

			GridRequest gridRequest = new GridRequest("176", "WSJOBS_CWO", "180");
			gridRequest.addParam("param.jobnum", workorder);

			List<ChildWorkOrder> childrenWOs = inforClient.getTools().getGridTools().convertGridResultToObject(ChildWorkOrder.class,
														map,
														inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequest));
			return ok(childrenWOs);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/equipment")
	
	public ResponseEntity<?> getWOEquipLinearDetails(@RequestParam("eqCode") String eqCode) throws InforException {
		try {
			final AdditionalWOEquipDetails woEquipLinearDetails = inforClient.getWorkOrderMiscService().getEquipLinearDetails(authenticationTools.getR5InforContext(), eqCode);
			return ok(woEquipLinearDetails);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/otherid/{workorder}")
	
	public ResponseEntity<?> getWOEqOtherIds(@PathVariable("workorder") String workorder) {
		try {
			GridRequest gridRequestWoEqOi = new GridRequest("UUOIEQ", 1000);
			gridRequestWoEqOi.addFilter("workorder", workorder, "EQUALS", GridRequestFilter.JOINER.AND);
			Map<String, String> eqToOtherId = inforClient.getTools().getGridTools().convertGridResultToMap("equipment", "otherid",
									inforClient.getGridsService().executeQuery(authenticationTools.getR5InforContext(), gridRequestWoEqOi));
			return ok(eqToOtherId);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}
}