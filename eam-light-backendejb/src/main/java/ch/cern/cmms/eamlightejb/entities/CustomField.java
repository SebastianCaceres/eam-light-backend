package ch.cern.cmms.eamlightejb.entities;

import lombok.Data;
import java.io.Serializable;

@Data
public class CustomField implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String label;
    private String value;
    private String type;
    private String defaultValue;
    private String mandatory;
}
