package ch.cern.cmms.eamlightejb.base.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "R5COMMENTS")
@IdClass(CommentId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @Column(name = "COM_CODE")
    private String keyValue;

    @Id
    @Column(name = "COM_ENTITY")
    private String entityCode;

    @Id
    @Column(name = "COM_LINE")
    private Integer line;

    @Column(name = "COM_TEXT")
    private String text;

    @Column(name = "COM_USER")
    private String user;

    @Column(name = "COM_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}
