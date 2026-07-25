package ch.cern.cmms.eamlightejb.watchers.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "U5WATCHERSNOTIFY")
@IdClass(WatcherId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatcherEntity {

    @Id
    @Column(name = "WAT_ENTITY")
    private String entity = "EVNT";

    @Id
    @Column(name = "WAT_TABLE")
    private String table = "R5EVENTS";

    @Id
    @Column(name = "WAT_PK_VALUE")
    private String workOrderCode;

    @Id
    @Column(name = "WAT_PERSON")
    private String person;

    @Column(name = "WAT_ORG")
    private String org = "*";

    @Column(name = "WAT_LINK")
    private String link = "*";
}
