package ch.cern.cmms.eamlightejb.workorders.repository;

import ch.cern.cmms.eamlightejb.workorders.entity.AdditionalCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdditionalCostRepository extends JpaRepository<AdditionalCostEntity, String> {
    List<AdditionalCostEntity> findByEventCode(String eventCode);
}
