package ch.cern.cmms.eamlightweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"ch.cern.cmms.eamlightweb", "ch.cern.cmms.eamlightejb"})
@EnableJpaRepositories(basePackages = {"ch.cern.cmms.eamlightejb"})
@EntityScan(basePackages = {
    "ch.cern.cmms.eamlightejb",
    "ch.cern.eam.wshub.core"
})
public class EamLightApplication {
    public static void main(String[] args) {
        SpringApplication.run(EamLightApplication.class, args);
    }
}
