package ch.cern.eam.wshub.core.services.equipment.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EquipmentGenerationEntity {
    @Id
    private String dummyId;

    public String getDummyId() {
        return dummyId;
    }

    public void setDummyId(String dummyId) {
        this.dummyId = dummyId;
    }
}
