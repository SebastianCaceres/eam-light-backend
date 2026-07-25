package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.PartUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartUsageRepository extends JpaRepository<PartUsageEntity, String> {
    List<PartUsageEntity> findByEventCode(String eventCode);
}
