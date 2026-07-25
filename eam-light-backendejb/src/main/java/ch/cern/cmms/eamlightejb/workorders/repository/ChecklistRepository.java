package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.ChecklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistRepository extends JpaRepository<ChecklistEntity, String> {
    List<ChecklistEntity> findByEventCodeOrderBySequenceAsc(String eventCode);
}
