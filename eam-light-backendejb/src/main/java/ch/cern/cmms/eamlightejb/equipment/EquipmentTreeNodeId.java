package ch.cern.cmms.eamlightejb.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentTreeNodeId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String parent;
}
