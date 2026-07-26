package ch.cern.cmms.eamlightweb.watchers;

import ch.cern.cmms.eamlightejb.watchers.WatchersService;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/workorders")
public class WatchersController extends EAMLightController {

    @Autowired
    private WatchersService watchersService;

    @GetMapping("/{woCode}/watchers")
    public ResponseEntity<?> getWatchersForWorkOrder(@PathVariable("woCode") String woCode) {
        try {
            return ok(watchersService.getWatchersForWorkOrder(authenticationTools.getInforContext(), woCode));
        } catch (InforException e){
            return forbidden(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PostMapping("/{woCode}/watchers")
    public ResponseEntity<?> addWatchersToWorkOrder(@PathVariable("woCode") String woCode, @RequestBody List<String> users) {
        try {
            return ok(watchersService.addWatchersToWorkOrder(authenticationTools.getInforContext(),
                    authenticationTools.getR5InforContext(), woCode, users));
        } catch (InforException e){
            return forbidden(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PutMapping("/{woCode}/watchers/remove")
    public ResponseEntity<?> removeWatchersFromWorkOrder(@PathVariable("woCode") String woCode, @RequestBody List<String> users) {
        try {
            return ok(watchersService.removeWatchersFromWorkOrder(authenticationTools.getInforContext(), woCode, users));
        } catch (InforException e){
            return forbidden(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

}
