package ch.cern.cmms.eamlightweb.workorders.partusage;

import ch.cern.cmms.eamlightweb.application.ApplicationService;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.plugins.SharedPlugin;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.entities.Pair;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestFilter;
import ch.cern.eam.wshub.core.services.material.entities.IssueReturnPartTransaction;
import ch.cern.eam.wshub.core.services.material.entities.IssueReturnPartTransactionLine;
import ch.cern.eam.wshub.core.services.material.entities.IssueReturnPartTransactionType;
import ch.cern.eam.wshub.core.services.workorders.entities.Activity;
import ch.cern.cmms.eamlightejb.entities.WorkOrder;
import ch.cern.eam.wshub.core.tools.GridTools;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static ch.cern.eam.wshub.core.tools.Tools.extractEntityCode;
import static ch.cern.eam.wshub.core.tools.Tools.extractOrganizationCode;

@RestController
@RequestMapping("/partusage")
public class PartUsageRest extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;
	@Autowired
	private SharedPlugin sharedPlugin;
	@Autowired
	private ApplicationService applicationService;

	@GetMapping("/bins")
	public ResponseEntity<?> loadBinList(@RequestParam("transaction") String transaction, @RequestParam("bin") String bin,
								@RequestParam("part") String part, @RequestParam("store") String store) {
		try {
			GridRequest gridRequest;
			if (transaction.startsWith("I")) {
				// ISSUE
				gridRequest = new GridRequest("LVISSBIN", GridRequest.GRIDTYPE.LOV);
				gridRequest.addParam("part_code", extractEntityCode(part));
				gridRequest.addParam("part_org", inforClient.getTools().getOrganizationCode(authenticationTools.getInforContext(), extractOrganizationCode(part)));
				gridRequest.addParam("store_code", store);
				if (bin != null && !bin.isEmpty()) {
					gridRequest.addFilter("bincode", bin.toUpperCase(), "BEGINS");
				}
			} else {
				// RETURN
				gridRequest = new GridRequest("LVRETBIN", GridRequest.GRIDTYPE.LOV);
				gridRequest.addParam("part_code", extractEntityCode(part));
				gridRequest.addParam("part_org", inforClient.getTools().getOrganizationCode(authenticationTools.getInforContext(), extractOrganizationCode(part)));
				gridRequest.addParam("store_code", store);
				if (bin != null && !bin.isEmpty()) {
					gridRequest.addFilter("bincode", bin.toUpperCase(), "BEGINS");
				}
			}

			return ok(GridTools.convertGridResultToObject(Pair.class,
					  Pair.generateGridPairMap("825", "2175"),
					  inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest)));

		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping("/lots/issue")
	public ResponseEntity<?> loadLotListIssue(@RequestParam("lot") String lot, @RequestParam("bin") String bin,
									 @RequestParam("part") String part, @RequestParam("store") String store,
									 @RequestParam("requireAvailableQty") boolean requireAvailableQty) {
		try {
			GridRequest gridRequest;
			InforContext context = authenticationTools.getInforContext();

			gridRequest = new GridRequest("LVIRLOT", GridRequest.GRIDTYPE.LOV);
			System.out.println("part: " +  part);
			gridRequest.addParam("bin_code", bin);
			gridRequest.addParam("part_code", extractEntityCode(part));
			gridRequest.addParam("part_org", inforClient.getTools().getOrganizationCode(authenticationTools.getInforContext(), extractOrganizationCode(part)));
			gridRequest.addParam("store_code", store);

			if (requireAvailableQty) {
				gridRequest.addFilter("availableqty", "0", ">", GridRequestFilter.JOINER.AND);
			}

			if (lot != null && !lot.isEmpty()) {
				gridRequest.addFilter("lotcode", lot, "=");
			}

			return ok(GridTools.convertGridResultToObject(Pair.class,
					  Pair.generateGridPairMap("825", "2175"),
					  inforClient.getGridsService().executeQuery(context, gridRequest)));

		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping("/lots/return")
	public ResponseEntity<?> loadLotListReturn(@RequestParam("lot") String lot, @RequestParam("part") String part) {
		try {
			GridRequest gridRequest;
			InforContext context = authenticationTools.getInforContext();
			Map<String, String> applicationData = applicationService.getParams(context.getTenant());

			List<Pair> udsLots = sharedPlugin.getUdsLots(extractEntityCode(part), inforClient, context, applicationData);

			// Check whether there are user defined lots, otherwise return all lots
			if (udsLots != null && !udsLots.isEmpty()) {
				return ok(udsLots);
			} else {
				gridRequest = new GridRequest("LVLOT", GridRequest.GRIDTYPE.LOV);
				gridRequest.setRowCount(10000);
			}

			if (lot != null && !lot.isEmpty()) {
				gridRequest.addFilter("lotcode", lot, "=");
			}

			return ok(GridTools.convertGridResultToObject(Pair.class,
					  Pair.generateGridPairMap("2174", "2175"),
					  inforClient.getGridsService().executeQuery(context, gridRequest)));

		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping("/transaction")
	public ResponseEntity<?> createPartUsage(@RequestBody IssueReturnPartTransaction transaction) {
		try {
			transaction.setTransactionOn(IssueReturnPartTransactionType.WORKORDER);
			return ok(inforClient.getPartMiscService().createIssueReturnTransaction(authenticationTools.getInforContext(), transaction));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping("/init")
	public ResponseEntity<?> initPartUsage(@RequestBody WorkOrder workOrder) {
		try {
			// Create the issue/return transacction for the workOrder
			IssueReturnPartTransaction transaction = createTransaction(workOrder);
			// Create the transaction line and add it to the transaction
			IssueReturnPartTransactionLine transLine = createTransactionLine();
			// Add the line to the transaction
			transaction.getTransactionlines().add(transLine);
			return ok(transaction);
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	/**
	 * Creates the transaction line that is going to be added to the transaction
	 *
	 * @return The transaction line to be created
	 */
	private IssueReturnPartTransactionLine createTransactionLine() {
		// Init the line
		IssueReturnPartTransactionLine line = new IssueReturnPartTransactionLine();
		/* Assign attributes */
		// Part Code
		line.setPartCode(null);
		// Part Org
		line.setPartOrg("*");
		// Bin
		line.setBin(null);
		// Lot
		line.setLot(null);
		// Quantity
		line.setTransactionQty(new BigDecimal(1));
		// Asset code
		line.setAssetIDCode(null);
		// Returns line created
		return line;
	}

	/**
	 * Creates the transaction
	 *
	 * @return
	 * @throws InforException
	 */
	private IssueReturnPartTransaction createTransaction(WorkOrder workOrder) throws InforException {
		// Create the transaction
		IssueReturnPartTransaction transaction = new IssueReturnPartTransaction();
		/* Assign attributes */
		// Transaction Type
		transaction.setTransactionOn(IssueReturnPartTransactionType.WORKORDER);
		// Work order number
		transaction.setWorkOrderNumber(workOrder.getNumber());
		// Equipment
		transaction.setEquipmentCode(workOrder.getEquipmentCode());
		// Activity
		// Read activities
		Activity[] activities = inforClient.getLaborBookingService().readActivities(authenticationTools.getInforContext(), workOrder.getNumber(), false);
		if (activities != null && activities.length == 1) {
			transaction.setActivityCode(activities[0].getActivityCode().toString());
		} else {
			transaction.setActivityCode(null);
		}
		// Store
		transaction.setStoreCode(null);
		// Department
		transaction.setDepartmentCode(workOrder.getDepartmentCode());
		// Transaction type
		transaction.setTransactionType("ISSUE");
		// Init lines
		transaction.setTransactionlines(new LinkedList<IssueReturnPartTransactionLine>());
		// Returns the transaction created
		return transaction;
	}

}
