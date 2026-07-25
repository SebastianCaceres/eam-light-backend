package ch.cern.cmms.eamlightejb.watchers.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatcherId implements Serializable {
    private String entity;
    private String table;
    private String workOrderCode;
    private String person;
}
