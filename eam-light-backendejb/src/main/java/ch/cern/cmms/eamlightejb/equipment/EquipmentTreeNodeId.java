package ch.cern.cmms.eamlightejb.equipment;

import java.io.Serializable;
import java.util.Objects;

public class EquipmentTreeNodeId implements Serializable {
    private String id;
    private String parent;

    public EquipmentTreeNodeId() {}

    public EquipmentTreeNodeId(String id, String parent) {
        this.id = id;
        this.parent = parent;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParent() { return parent; }
    public void setParent(String parent) { this.parent = parent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentTreeNodeId that = (EquipmentTreeNodeId) o;
        return Objects.equals(id, that.id) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parent);
    }
}
