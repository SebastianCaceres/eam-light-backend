package ch.cern.cmms.eamlightweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ch.cern.cmms.eamlightweb", "ch.cern.cmms.eamlightejb"})
public class EamLightApplication {
    public static void main(String[] args) {
        SpringApplication.run(EamLightApplication.class, args);
    }
}
