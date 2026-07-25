package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "R5BOOKEDLABOUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabourBookingEntity {

    @Id
    @Column(name = "BOO_CODE")
    private String code;

    @Column(name = "BOO_EVENT")
    private String eventCode;

    @Column(name = "BOO_HOURS")
    private Double hours;

    @Column(name = "BOO_DATE")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "BOO_PERSON")
    private String person;
}
