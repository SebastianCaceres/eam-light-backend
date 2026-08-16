package ch.cern.cmms.eamlightejb.data;

import ch.cern.eam.wshub.core.repositories.ActivityRepository;
import ch.cern.eam.wshub.core.repositories.CommentRepository;
import ch.cern.eam.wshub.core.repositories.LaborBookingRepository;
import ch.cern.eam.wshub.core.repositories.LotRepository;
import ch.cern.eam.wshub.core.repositories.NonConformityRepository;
import ch.cern.eam.wshub.core.repositories.WorkOrderRepository;
import ch.cern.eam.wshub.core.services.comments.entities.Comment;
import ch.cern.eam.wshub.core.services.equipment.entities.NonConformity;
import ch.cern.eam.wshub.core.services.material.entities.Lot;
import ch.cern.eam.wshub.core.services.workorders.entities.Activity;
import ch.cern.eam.wshub.core.services.workorders.entities.LaborBooking;
import ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import ch.cern.eam.wshub.core.repositories.ScreenLayoutRepository;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayoutEntity;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayout;
import ch.cern.eam.wshub.core.services.administration.entities.ElementInfo;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
@Profile("local")
public class LocalDataInitializer {

    @Autowired(required = false)
    private WorkOrderRepository workOrderRepository;

    @Autowired(required = false)
    private ActivityRepository activityRepository;

    @Autowired(required = false)
    private LaborBookingRepository laborBookingRepository;

    @Autowired(required = false)
    private CommentRepository commentRepository;

    @Autowired(required = false)
    private NonConformityRepository nonConformityRepository;

    @Autowired(required = false)
    private LotRepository lotRepository;

    @Autowired(required = false)
    private ch.cern.eam.wshub.core.repositories.BinRepository binRepository;

    @Autowired(required = false)
    private ScreenLayoutRepository screenLayoutRepository;

    @Autowired
    private ObjectMapper objectMapper;


    private ScreenLayoutEntity createLayoutEntity(String id, ScreenLayout layout) {
        ScreenLayoutEntity entity = new ScreenLayoutEntity();
        entity.setLayoutId(id);
        try {
            entity.setLayoutJson(objectMapper.writeValueAsString(layout));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    private ScreenLayout createBaseLayout() {
        ScreenLayout layout = new ScreenLayout();
        java.util.Map<String, ElementInfo> fieldMap = new java.util.HashMap<>();

        for (int i = 1; i <= 35; i++) {
            ElementInfo info1 = new ElementInfo();
            info1.setAttribute("O");
            info1.setText("Block " + i);
            info1.setOnLookup("{\"lovName\":\"LVDEFAULT\",\"inputVars\":{},\"inputFields\":{},\"returnFields\":{}}");
            fieldMap.put("block_" + i, info1);
            fieldMap.put("BLOCK_" + i, info1);
        }
        String[] commonFields = {
            "status", "statusCode", "STATUSCODE", "type", "equipment", "department",
            "organization", "location", "parentlocation", "part", "ncr", "workorder", "asset",
            "parentasset", "position", "system", "primarysystem", "customFields", "userDefinedFields",
            "udfchar11", "udfchar12", "udfchar13", "udfchar14", "udfchar15",
            "udfchkbox01", "UDFCHKBOX01", "workordernum", "workOrdernum", "WORKORDERNUM",
            "partcode", "PARTCODE", "equipmentcode", "EQUIPMENTCODE", "locationcode", "LOCATIONCODE",
            "lotcode", "LOTCODE", "store", "STORE", "partdesc", "PARTDESC", "quantity", "QUANTITY",
            "uom", "UOM", "transactiontype", "TRANSACTIONTYPE", "partusage", "PARTUSAGE", "activity", "ACTIVITY",
            "storecode", "STORECODE", "assetid", "ASSETID", "bincode", "BINCODE",
            "transactionquantity", "TRANSACTIONQUANTITY", "costcode", "assignedto"
        };
        for (String cf : commonFields) {
            ElementInfo info = new ElementInfo();
            info.setAttribute("O");
            String capitalized = cf.substring(0, 1).toUpperCase() + cf.substring(1).toLowerCase();
            info.setText(capitalized);
            info.setOnLookup("{\"lovName\":\"LVDEFAULT\",\"inputVars\":{},\"inputFields\":{},\"returnFields\":{}}");
            fieldMap.put(cf, info);
        }

        layout.setFields(fieldMap);

        java.util.Map<String, ch.cern.eam.wshub.core.services.administration.entities.Tab> tabMap = new java.util.HashMap<>();
        ch.cern.eam.wshub.core.services.administration.entities.Tab defaultTab = new ch.cern.eam.wshub.core.services.administration.entities.Tab();
        defaultTab.setTabAvailable(true);
        defaultTab.setAlwaysDisplayed(true);
        defaultTab.setTabDescription("Tab");
        defaultTab.setFields(fieldMap);
        tabMap.put("fields", defaultTab);

        String[] tabCodes = {
            "HDR", "EVT", "CMT", "CLO", "PAS", "UT1", "UT2", "UT5", "BIS", "EPA",
            "ACT", "ACK", "PAR", "REA", "MEC", "CWO", "DOC", "BOO", "ACO", "ESF",
            "OBS", "NCF", "NCT", "CLOSING_CODES", "HEADER", "WORKORDER", "COMMENTS",
            "PARTS", "DOCUMENTS", "ACTIVITIES", "BOOK_LABOR", "CUSTOMFIELDSSECTION", "CHECKLISTS"
        };
        for (String tc : tabCodes) {
            tabMap.put(tc, defaultTab);
        }

        for (int i = 1; i <= 35; i++) {
            tabMap.put("TAB_" + i, defaultTab);
            tabMap.put("tab_" + i, defaultTab);
        }

        layout.setTabs(tabMap);
        return layout;
    }

    @PostConstruct
    public void initData() {
        try {
            // Seed Work Order 10001 if missing
            if (workOrderRepository != null && !workOrderRepository.existsById("10001")) {
                WorkOrder wo = new WorkOrder();
                wo.setNumber("10001");
                wo.setDescription("Sample Work Order 10001");
                wo.setEquipmentCode("AST-001");
                wo.setStatusCode("R");
                wo.setTypeCode("JOB");
                wo.setDepartmentCode("DEP1");
                workOrderRepository.save(wo);
            }

            // Seed Activity 10 for WO 10001 if missing
            if (activityRepository != null) {
                List<Activity> activities = activityRepository.findByWorkOrder("10001");
                if (activities == null || activities.isEmpty()) {
                    Activity act = new Activity();
                    act.setWorkOrderNumber("10001");
                    act.setActivityCode(new BigInteger("10"));
                    act.setNote("Inspect equipment and replace filters");
                    activityRepository.save(act);
                }
            }

            // Seed Labor Booking for WO 10001 if missing
            if (laborBookingRepository != null) {
                List<LaborBooking> bookings = laborBookingRepository.findByWorkOrder("10001");
                if (bookings == null || bookings.isEmpty()) {
                    LaborBooking booking = new LaborBooking();
                    booking.setCode("1001");
                    booking.setWorkOrderNumber("10001");
                    booking.setActivityCode("10");
                    booking.setEmployeeCode("ADMIN");
                    booking.setHoursWorked(new BigDecimal(4));
                    laborBookingRepository.save(booking);
                }
            }

            // Seed Comment for WO 10001 if missing
            if (commentRepository != null) {
                List<Comment> comments = commentRepository.findByEntityCodeAndEntityKeyCode("EVNT", "10001");
                if (comments == null || comments.isEmpty()) {
                    Comment comment = new Comment();
                    comment.setPk("CMT-10001-1");
                    comment.setEntityCode("EVNT");
                    comment.setEntityKeyCode("10001");
                    comment.setLineNumber("1");
                    comment.setText("Work Order created for local profile testing");
                    comment.setCreationUserCode("ADMIN");
                    comment.setCreationDate("01-JAN-2026");
                    commentRepository.save(comment);
                }
            }

            // Seed NCR NCR-001 if missing
            if (nonConformityRepository != null && !nonConformityRepository.existsById("NCR-001")) {
                NonConformity ncr = new NonConformity();
                ncr.setCode("NCR-001");
                ncr.setDescription("Minor pressure drop observed");
                ncr.setTypeCode("OBSERVATION");
                ncr.setStatusCode("OPEN");
                nonConformityRepository.save(ncr);
            }

            // Seed Lot LOT-001 if missing
            if (lotRepository != null && !lotRepository.existsById("LOT-001")) {
                Lot lot = new Lot();
                lot.setCode("LOT-001");
                lot.setDesc("Standard Stock Lot 001");
                lotRepository.save(lot);
            }

            // Seed Bin BIN-001 if missing
            if (binRepository != null && !binRepository.existsById("BIN-001")) {
                ch.cern.eam.wshub.core.services.material.entities.Bin b = new ch.cern.eam.wshub.core.services.material.entities.Bin();
                b.setBinCode("BIN-001");
                b.setStoreCode("ST01");
                b.setBinDesc("Main Store Bin 001");
                binRepository.save(b);
            }

            
            // Seed Role-Based Screen Layouts
            if (screenLayoutRepository != null) {
                // 1. DEFAULT_LAYOUT (Fallback)
                if (!screenLayoutRepository.existsById("DEFAULT_LAYOUT")) {
                    screenLayoutRepository.save(createLayoutEntity("DEFAULT_LAYOUT", createBaseLayout()));
                }
                
                // 2. WORKER_WSJOBS Layout
                if (!screenLayoutRepository.existsById("WORKER_WSJOBS")) {
                    ScreenLayout workerLayout = createBaseLayout();
                    
                    // Worker constraints: Cost Code hidden, Assignee read-only, UDFs hidden
                    if (workerLayout.getFields().containsKey("costcode")) {
                        workerLayout.getFields().get("costcode").setAttribute("H"); // Hidden
                    }
                    if (workerLayout.getFields().containsKey("assignedto")) {
                        workerLayout.getFields().get("assignedto").setAttribute("P"); // Protected/Read-Only
                    }
                    // Hide Custom Fields tab
                    if (workerLayout.getTabs().containsKey("CUSTOMFIELDSSECTION")) {
                        workerLayout.getTabs().get("CUSTOMFIELDSSECTION").setTabAvailable(false);
                        workerLayout.getTabs().get("CUSTOMFIELDSSECTION").setAlwaysDisplayed(false);
                    }
                    // Make Checklists mandatory (just an example, usually 'R' means required)
                    if (workerLayout.getTabs().containsKey("CHECKLISTS")) {
                        workerLayout.getTabs().get("CHECKLISTS").setTabAvailable(true);
                        workerLayout.getTabs().get("CHECKLISTS").setAlwaysDisplayed(true);
                    }
                    
                    screenLayoutRepository.save(createLayoutEntity("WORKER_WSJOBS", workerLayout));
                }
                
                // 3. SUPERVISOR_WSJOBS Layout
                if (!screenLayoutRepository.existsById("SUPERVISOR_WSJOBS")) {
                    ScreenLayout supervisorLayout = createBaseLayout();
                    
                    // Supervisor capabilities: Cost Code optional (can edit), Assignee optional (can reassign), UDFs available
                    if (supervisorLayout.getFields().containsKey("costcode")) {
                        supervisorLayout.getFields().get("costcode").setAttribute("O"); // Optional
                    }
                    if (supervisorLayout.getFields().containsKey("assignedto")) {
                        supervisorLayout.getFields().get("assignedto").setAttribute("O"); // Optional
                    }
                    if (supervisorLayout.getTabs().containsKey("CUSTOMFIELDSSECTION")) {
                        supervisorLayout.getTabs().get("CUSTOMFIELDSSECTION").setTabAvailable(true);
                        supervisorLayout.getTabs().get("CUSTOMFIELDSSECTION").setAlwaysDisplayed(true);
                    }
                    
                    screenLayoutRepository.save(createLayoutEntity("SUPERVISOR_WSJOBS", supervisorLayout));
                }
            }


        } catch (Exception e) {
            System.out.println("LocalDataInitializer warning: " + e.getMessage());
        }
    }
}
