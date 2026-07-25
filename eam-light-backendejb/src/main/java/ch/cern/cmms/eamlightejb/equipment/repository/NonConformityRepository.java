package ch.cern.cmms.eamlightejb.equipment.repository;

import ch.cern.cmms.eamlightejb.equipment.entity.NonConformityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NonConformityRepository extends JpaRepository<NonConformityEntity, String> {
    List<NonConformityEntity> findByEquipmentCode(String equipmentCode);
}
