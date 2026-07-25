package ch.cern.cmms.eamlightejb.equipment.repository;

import ch.cern.cmms.eamlightejb.equipment.EquipmentTreeNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentTreeNodeRepository extends JpaRepository<EquipmentTreeNode, String> {

    @Query(name = EquipmentTreeNode.GET_TREE, nativeQuery = true)
    List<EquipmentTreeNode> getTree(@Param("equipment") String equipment);
}
