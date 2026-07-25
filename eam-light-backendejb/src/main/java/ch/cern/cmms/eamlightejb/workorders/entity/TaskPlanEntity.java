package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5TASKPLANS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPlanEntity {

    @Id
    @Column(name = "TKP_CODE")
    private String code;

    @Column(name = "TKP_DESC")
    private String description;

    @Column(name = "TKP_DURATION")
    private Double duration;
}
