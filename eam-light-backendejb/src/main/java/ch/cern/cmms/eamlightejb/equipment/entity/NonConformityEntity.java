package ch.cern.cmms.eamlightejb.equipment.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5NONCONFORMITIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonConformityEntity {

    @Id
    @Column(name = "NCF_CODE")
    private String code;

    @Column(name = "NCF_DESC")
    private String description;

    @Column(name = "NCF_EQUIPMENT")
    private String equipmentCode;
}
