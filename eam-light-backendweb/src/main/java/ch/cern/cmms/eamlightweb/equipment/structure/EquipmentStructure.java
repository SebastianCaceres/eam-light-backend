package ch.cern.cmms.eamlightweb.equipment.structure;

import ch.cern.cmms.eamlightejb.equipment.EquipmentEJB;
import ch.cern.cmms.eamlightejb.equipment.tools.GraphNode;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class EquipmentStructure
{
    @Autowired
    private EquipmentEJB equipmentEJB;
    @Autowired
    private AuthenticationTools authenticationTools;
    @Autowired
    private InforClient inforClient;

    public List<GraphNode> readEquipmentTree(String eqID, String org, String type) throws InforException {
        try {
            if (equipmentEJB != null) {
                List<GraphNode> tree = equipmentEJB.getEquipmentStructureTree(eqID);
                if (tree != null && !tree.isEmpty()) {
                    return tree;
                }
            }
        } catch (Exception ignored) {}

        GraphNode root = new GraphNode();
        root.setId(eqID);
        root.setType(type);
        root.setIdOrg(org);
        return new LinkedList<>(Arrays.asList(root));
    }
}
