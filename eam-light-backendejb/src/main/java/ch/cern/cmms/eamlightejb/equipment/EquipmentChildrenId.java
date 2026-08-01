package ch.cern.cmms.eamlightejb.equipment;

import java.io.Serializable;
import java.util.Objects;

public class EquipmentChildrenId implements Serializable {
    private String parentCode;
    private String childCode;
    private String parentType;
    private String childType;

    public EquipmentChildrenId() {}

    public EquipmentChildrenId(String parentCode, String childCode, String parentType, String childType) {
        this.parentCode = parentCode;
        this.childCode = childCode;
        this.parentType = parentType;
        this.childType = childType;
    }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public String getChildCode() { return childCode; }
    public void setChildCode(String childCode) { this.childCode = childCode; }

    public String getParentType() { return parentType; }
    public void setParentType(String parentType) { this.parentType = parentType; }

    public String getChildType() { return childType; }
    public void setChildType(String childType) { this.childType = childType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentChildrenId that = (EquipmentChildrenId) o;
        return Objects.equals(parentCode, that.parentCode) &&
               Objects.equals(childCode, that.childCode) &&
               Objects.equals(parentType, that.parentType) &&
               Objects.equals(childType, that.childType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentCode, childCode, parentType, childType);
    }
}
