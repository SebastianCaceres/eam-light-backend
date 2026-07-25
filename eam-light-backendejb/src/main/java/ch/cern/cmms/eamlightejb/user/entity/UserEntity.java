package ch.cern.cmms.eamlightejb.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "R5USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "USR_CODE")
    private String code;

    @Column(name = "USR_DESC")
    private String description;

    @Column(name = "USR_EMAILADDRESS")
    private String emailAddress;
}
