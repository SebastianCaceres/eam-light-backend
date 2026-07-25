package ch.cern.cmms.eamlightejb.equipment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5NCROBSERVATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NCRObservationEntity {

    @Id
    @Column(name = "NCO_CODE")
    private String code;

    @Column(name = "NCO_NCF")
    private String nonConformityCode;

    @Column(name = "NCO_DESC")
    private String description;
}
