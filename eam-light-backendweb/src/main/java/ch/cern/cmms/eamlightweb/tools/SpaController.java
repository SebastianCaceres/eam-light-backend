package ch.cern.cmms.eamlightweb.tools;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "", "/",
        "/wosearch", "/workorder", "/workorder/**",
        "/assetsearch", "/positionsearch", "/systemsearch", "/locationsearch",
        "/partsearch", "/part", "/part/**", "/lotsearch",
        "/ncrsearch", "/ncr", "/ncr/**",
        "/replaceeqp", "/meterreading"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
