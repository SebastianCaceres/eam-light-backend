package ch.cern.cmms.eamlightejb.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentChildrenId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String parentCode;
    private String childCode;
    private String parentType;
    private String childType;
}
