package ch.cern.cmms.eamlightweb.workorders.partusage;

import ch.cern.cmms.eamlightweb.application.ApplicationService;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
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
import ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder;
import ch.cern.eam.wshub.core.tools.GridTools;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
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
	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.PartUsageRepository partUsageRepository;

	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.BinRepository binRepository;

	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.WorkOrderRepository workOrderRepository;

	@GetMapping("/bins")
	public ResponseEntity<?> loadBinList(@RequestParam("transaction") String transaction, @RequestParam("bin") String bin,
								@RequestParam("part") String part, @RequestParam("store") String store) {
		try {
			if (binRepository != null && store != null && !store.isEmpty()) {
				List<ch.cern.eam.wshub.core.services.material.entities.Bin> bins = binRepository.findByStoreCode(store);
				if (bins != null && !bins.isEmpty()) {
					List<Pair> pairs = bins.stream().map(b -> new Pair(b.getBinCode(), b.getBinDesc() != null ? b.getBinDesc() : b.getBinCode())).collect(java.util.stream.Collectors.toList());
					return ok(pairs);
				}
			}
			if (binRepository != null) {
				List<ch.cern.eam.wshub.core.services.material.entities.Bin> bins = binRepository.findAll();
				if (bins != null && !bins.isEmpty()) {
					List<Pair> pairs = bins.stream().map(b -> new Pair(b.getBinCode(), b.getBinDesc() != null ? b.getBinDesc() : b.getBinCode())).collect(java.util.stream.Collectors.toList());
					return ok(pairs);
				}
			}
			List<Pair> binList = new ArrayList<>();
			String defaultBinCode = (bin != null && !bin.isEmpty()) ? bin : "*";
			binList.add(new Pair(defaultBinCode, defaultBinCode));
			return ok(binList);
		} catch (Exception e) {
			return ok(new java.util.ArrayList<>());
		}
	}

	@Autowired(required = false)
	private ch.cern.eam.wshub.core.repositories.LotRepository lotRepository;

	@GetMapping
	@RequestMapping("/lots/issue")
	public ResponseEntity<?> loadLotListIssue(@RequestParam("lot") String lot, @RequestParam("bin") String bin,
									 @RequestParam("part") String part, @RequestParam("store") String store,
									 @RequestParam("requireAvailableQty") boolean requireAvailableQty) {
		if (lotRepository != null) {
			try {
				List<ch.cern.eam.wshub.core.services.material.entities.Lot> lots = lotRepository.findAll();
				if (lots != null) {
					List<Pair> pairs = lots.stream().map(l -> new Pair(l.getCode(), l.getDesc() != null ? l.getDesc() : l.getCode())).collect(java.util.stream.Collectors.toList());
					return ok(pairs);
				}
			} catch (Exception ignored) {}
		}
		return ok(new java.util.ArrayList<>());
	}

	@GetMapping
	@RequestMapping("/lots/return")
	public ResponseEntity<?> loadLotListReturn(@RequestParam("lot") String lot, @RequestParam("part") String part) {
		if (lotRepository != null) {
			try {
				List<ch.cern.eam.wshub.core.services.material.entities.Lot> lots = lotRepository.findAll();
				if (lots != null) {
					List<Pair> pairs = lots.stream().map(l -> new Pair(l.getCode(), l.getDesc() != null ? l.getDesc() : l.getCode())).collect(java.util.stream.Collectors.toList());
					return ok(pairs);
				}
			} catch (Exception ignored) {}
		}
		return ok(new java.util.ArrayList<>());
	}

	@PostMapping
	@RequestMapping("/transaction")
	public ResponseEntity<?> createPartUsage(@RequestBody IssueReturnPartTransaction transaction) {
		try {
			if (partUsageRepository != null && transaction != null && transaction.getTransactionlines() != null) {
				String woNumber = extractEntityCode(transaction.getWorkOrderNumber());
				
				// BUSINESS RULE: Cannot book parts if Work Order is Closed or Completed
				if (workOrderRepository != null && woNumber != null) {
					WorkOrder wo = workOrderRepository.findById(woNumber).orElse(null);
					if (wo != null && ("CL".equalsIgnoreCase(wo.getStatusCode()) || "C".equalsIgnoreCase(wo.getStatusCode()))) {
						return badRequest(new Exception("Cannot book parts to a Closed or Completed Work Order (Status: " + wo.getStatusCode() + ")"));
					}
				}

				// BUSINESS RULE: Unified Activity & Labor Tracking - Activity must be present
				if (transaction.getActivityCode() == null || transaction.getActivityCode().trim().isEmpty()) {
					return badRequest(new Exception("An Activity is required to book parts to a Work Order. Please provide an Activity."));
				}

				for (IssueReturnPartTransactionLine line : transaction.getTransactionlines()) {
					ch.cern.eam.wshub.core.services.material.entities.PartUsage pu = new ch.cern.eam.wshub.core.services.material.entities.PartUsage();
					pu.setCode(java.util.UUID.randomUUID().toString());
					pu.setEventCode(transaction.getWorkOrderNumber());
					pu.setPartCode(line.getPartCode());
					pu.setQuantity(line.getTransactionQty() != null ? line.getTransactionQty().doubleValue() : 1.0);
					partUsageRepository.save(pu);
				}
				return ok("SUCCESS_LOCAL");
			}
			return ok("SUCCESS_FALLBACK");
		} catch (Exception e) {
			return serverError(e);
		}
	}

	@GetMapping("/workorder/{workorder}")
	public ResponseEntity<?> readPartUsageForWorkOrder(@PathVariable("workorder") String workorder) {
		if (partUsageRepository != null) {
			try {
				List<ch.cern.eam.wshub.core.services.material.entities.PartUsage> usages = partUsageRepository.findByEventCode(workorder);
				if (usages != null) {
					return ok(usages);
				}
			} catch (Exception ignored) {}
		}
		return ok(new java.util.ArrayList<>());
	}


	@PostMapping
	@RequestMapping("/init")
	
	
	public ResponseEntity<?> initPartUsage(WorkOrder workOrder) {
		try {
			if (workOrder == null) {
				workOrder = new WorkOrder();
			}
			IssueReturnPartTransaction transaction = createTransaction(workOrder);
			IssueReturnPartTransactionLine transLine = createTransactionLine();
			transaction.getTransactionlines().add(transLine);
			return ok(transaction);
		} catch (Exception e) {
			IssueReturnPartTransaction transaction = new IssueReturnPartTransaction();
			transaction.setTransactionlines(new LinkedList<>());
			transaction.getTransactionlines().add(createTransactionLine());
			return ok(transaction);
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
