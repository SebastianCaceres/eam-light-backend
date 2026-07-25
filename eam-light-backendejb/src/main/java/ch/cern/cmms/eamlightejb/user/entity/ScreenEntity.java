package ch.cern.cmms.eamlightejb.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5SCREENS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenEntity {

    @Id
    @Column(name = "SCR_CODE")
    private String code;

    @Column(name = "SCR_DESC")
    private String description;
}
