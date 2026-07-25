package ch.cern.cmms.eamlightweb.login;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import javax.enterprise.context.RequestScoped;
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

    @GetMapping
    @RequestMapping("/")
    
    public ResponseEntity<?> login() throws InforException {
        try {
            InforContext inforContext = authenticationTools.getInforContext();
            inforContext.setKeepSession(true);
            return ok(inforClient.getUserSetupService().login(inforContext, ""));
        } catch (InforException e) {
            return badRequest(e);
        } catch(Exception e) {
            return serverError(e);
        }
    }

}
