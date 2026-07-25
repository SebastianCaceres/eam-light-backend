package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "R5EVENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderEntity {

    @Id
    @Column(name = "EVT_CODE")
    private String code;

    @Column(name = "EVT_DESC")
    private String description;

    @Column(name = "EVT_TYPE")
    private String type;

    @Column(name = "EVT_STATUS")
    private String status;

    @Column(name = "EVT_OBJECT")
    private String equipmentCode;

    @Column(name = "EVT_TARGET")
    @Temporal(TemporalType.TIMESTAMP)
    private Date targetDate;
}
