package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.LabourBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabourBookingRepository extends JpaRepository<LabourBookingEntity, String> {
    List<LabourBookingEntity> findByEventCodeOrderByDateDesc(String eventCode);
}
