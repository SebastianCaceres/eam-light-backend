package ch.cern.cmms.eamlightejb.workorders.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5CHECKLISTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistEntity {

    @Id
    @Column(name = "CKL_CODE")
    private String code;

    @Column(name = "CKL_EVENT")
    private String eventCode;

    @Column(name = "CKL_SEQUENCE")
    private Integer sequence;

    @Column(name = "CKL_DESC")
    private String description;

    @Column(name = "CKL_COMPLETED")
    private String completed;
}
