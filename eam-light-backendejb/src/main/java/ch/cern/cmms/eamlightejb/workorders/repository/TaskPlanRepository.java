package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.TaskPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskPlanRepository extends JpaRepository<TaskPlanEntity, String> {
}
