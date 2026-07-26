package ch.cern.eam.wshub.core.services.material.entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PhysicalInventoryRow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String dummyId;

    public String getDummyId() {
        return dummyId;
    }

    public void setDummyId(String dummyId) {
        this.dummyId = dummyId;
    }
}
