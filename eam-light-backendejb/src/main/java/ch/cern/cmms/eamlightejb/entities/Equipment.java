package ch.cern.cmms.eamlightejb.entities;

import lombok.Data;
import java.io.Serializable;

@Data
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String description;
    private String statusCode;
    private String typeCode;
    private String storeCode;
    private String departmentCode;
    private String partCode;
    private String bin;
    private String lot;
    private String stateCode;
    private String hierarchyPositionCode;

    private UserDefinedFields userDefinedFields;
    private CustomField[] customFields;
}
