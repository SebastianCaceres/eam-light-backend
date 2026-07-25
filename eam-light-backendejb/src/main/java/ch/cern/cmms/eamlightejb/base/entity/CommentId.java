package ch.cern.cmms.eamlightejb.base.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentId implements Serializable {
    private String keyValue;
    private String entityCode;
    private Integer line;
}
