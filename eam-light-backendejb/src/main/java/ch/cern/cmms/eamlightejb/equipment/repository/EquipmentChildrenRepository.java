package ch.cern.cmms.eamlightejb.equipment.repository;

import ch.cern.cmms.eamlightejb.equipment.EquipmentChildren;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentChildrenRepository extends JpaRepository<EquipmentChildren, String> {

    @Query(name = EquipmentChildren.GET_EQUIPMENT_CHILDREN, nativeQuery = true)
    List<EquipmentChildren> getEquipmentChildren(@Param("equipment") String equipment);

    @Query(name = EquipmentChildren.GET_EQUIPMENT_PARENTS, nativeQuery = true)
    List<EquipmentChildren> getEquipmentParents(@Param("equipment") String equipment);
}
