package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.WorkOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, String> {
    List<WorkOrderEntity> findByEquipmentCode(String equipmentCode);
}
