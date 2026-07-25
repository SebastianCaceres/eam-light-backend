package ch.cern.cmms.eamlightejb.equipment.repository;

import ch.cern.cmms.eamlightejb.equipment.entity.NCRObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NCRObservationRepository extends JpaRepository<NCRObservationEntity, String> {
    List<NCRObservationEntity> findByNonConformityCode(String nonConformityCode);
}
