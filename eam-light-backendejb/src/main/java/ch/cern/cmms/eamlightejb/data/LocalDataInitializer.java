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
        } catch (Exception e) {
            System.out.println("LocalDataInitializer warning: " + e.getMessage());
        }
    }
}
