package ch.cern.cmms.eamlightejb.workorders;

import ch.cern.cmms.eamlightejb.workorders.entity.WorkOrderEntity;
import ch.cern.cmms.eamlightejb.workorders.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WorkOrderEJB {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<WorkOrderEntity> readWorkOrder(String code) {
        return workOrderRepository.findById(code);
    }

    @Transactional
    public WorkOrderEntity createWorkOrder(WorkOrderEntity wo) {
        String code = getNextWorkOrderCode();
        wo.setCode(code);
        wo.setType("JOB");
        if (wo.getStatus() == null) {
            wo.setStatus("R"); // Requested status default
        }
        return workOrderRepository.save(wo);
    }

    @Transactional
    public WorkOrderEntity updateWorkOrder(WorkOrderEntity wo) {
        return workOrderRepository.save(wo);
    }

    @Transactional
    public void deleteWorkOrder(String code) {
        workOrderRepository.deleteById(code);
    }

    private String getNextWorkOrderCode() {
        try {
            return jdbcTemplate.queryForObject("SELECT SQ_EVT_CODE.NEXTVAL FROM DUAL", String.class);
        } catch (Exception e) {
            try {
                String maxCodeVal = jdbcTemplate.queryForObject(
                    "SELECT CAST(MAX(CAST(EVT_CODE AS INT)) + 1 AS VARCHAR) FROM R5EVENTS WHERE EVT_CODE REGEXP '^[0-9]+$'", 
                    String.class
                );
                return maxCodeVal != null ? maxCodeVal : "100000";
            } catch (Exception ex) {
                return String.valueOf(System.currentTimeMillis() / 1000);
            }
        }
    }
}
