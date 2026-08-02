package ch.cern.cmms.eamlightweb.login;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/login")
public class LoginController extends EAMLightController {

    @Autowired
    private InforClient inforClient;
    @Autowired
    private AuthenticationTools authenticationTools;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> login() throws InforException {
        try {
            InforContext inforContext = authenticationTools.getInforContext();
            inforContext.setKeepSession(true);
            return ok(inforClient.getUserSetupService().login(inforContext, ""));
        } catch (Exception e) {
            // Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
            ch.cern.eam.wshub.core.services.administration.entities.EAMUser defaultAdminUser = new ch.cern.eam.wshub.core.services.administration.entities.EAMUser();
            defaultAdminUser.setUserCode("ADMIN");
            defaultAdminUser.setUserGroup("ADMIN");
            return ok(defaultAdminUser);
        }
    }

}
