package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5ACTIVITIES")
@IdClass(ActivityId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity {

    @Id
    @Column(name = "ACT_EVENT")
    private String eventCode;

    @Id
    @Column(name = "ACT_ACT")
    private Integer activityNumber;

    @Column(name = "ACT_NOTE")
    private String note;

    @Column(name = "ACT_EST")
    private Double estimatedHours;
}
