package ch.cern.cmms.eamlightweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"ch.cern.cmms.eamlightweb", "ch.cern.cmms.eamlightejb", "ch.cern.eam.wshub.core"})
@EntityScan(basePackages = {
    "ch.cern.cmms.eamlightejb",
    "ch.cern.eam.wshub.core.services.workorders.entities",
    "ch.cern.eam.wshub.core.services.material.entities",
    "ch.cern.eam.wshub.core.services.equipment.entities",
    "ch.cern.eam.wshub.core.services.administration.entities",
    "ch.cern.eam.wshub.core.services.documents.entities",
    "ch.cern.eam.wshub.core.services.comments.entities",
    "ch.cern.eam.wshub.core.services.entities",
    "ch.cern.eam.wshub.core.services.grids.entities",
    "ch.cern.eam.wshub.core.services.grids.customfields"
})
@EnableJpaRepositories(basePackages = {"ch.cern.eam.wshub.core.repositories", "ch.cern.cmms.eamlightejb"})
public class EamLightApplication {
    public static void main(String[] args) {
        SpringApplication.run(EamLightApplication.class, args);
    }
}
