package ch.cern.cmms.eamlightweb.tools;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "", "/",
        "/wosearch", "/workorder", "/workorder/**",
        "/assetsearch", "/asset", "/asset/**",
        "/positionsearch", "/position", "/position/**",
        "/systemsearch", "/system", "/system/**",
        "/locationsearch", "/location", "/location/**",
        "/partsearch", "/part", "/part/**", "/lotsearch", "/lot", "/lot/**",
        "/ncrsearch", "/ncr", "/ncr/**",
        "/replaceeqp", "/meterreading",
        "/grid", "/report", "/eqptree", "/releasenotes", "/equipment", "/equipment/**"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
