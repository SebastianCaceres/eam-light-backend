package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "R5ADDITIONALCOSTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalCostEntity {

    @Id
    @Column(name = "ADC_CODE")
    private String code;

    @Column(name = "ADC_EVENT")
    private String eventCode;

    @Column(name = "ADC_COST")
    private Double cost;

    @Column(name = "ADC_DATE")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "ADC_DESC")
    private String description;
}
