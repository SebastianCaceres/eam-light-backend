package ch.cern.cmms.eamlightejb.entities;

import lombok.Data;
import java.io.Serializable;

@Data
public class WorkOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String number;
    private String description;
    private String equipmentCode;
    private String departmentCode;
}
