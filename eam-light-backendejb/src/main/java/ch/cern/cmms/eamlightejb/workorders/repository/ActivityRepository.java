package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.ActivityEntity;
import ch.cern.cmms.eamlightejb.workorders.entity.ActivityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, ActivityId> {
    List<ActivityEntity> findByEventCodeOrderByActivityNumberAsc(String eventCode);
}
