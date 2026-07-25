package ch.cern.cmms.eamlightweb.tools.autocomplete;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.cmms.eamlightejb.watchers.WatcherInfo;
import ch.cern.cmms.eamlightejb.watchers.WatchersService;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/autocomplete")

public class AutocompleteUser extends EAMLightController {

    @Autowired
    private WatchersService watchersService;

    @GetMapping
    @RequestMapping("/users/{code}")
    
    
    public ResponseEntity<?> complete(@PathVariable("code") String code) throws InforException {
        return ok(watchersService.getAutocompleteOptions(authenticationTools.getR5InforContext(), code));
    }

    @GetMapping
    @RequestMapping("/workorders/{wo}/users/")
    
    
    public ResponseEntity<?> completeFilteredByWOAccess(@RequestParam("hint") String hint, @PathVariable("wo") String woCode) {
        final List<WatcherInfo> filteredWatcherInfo = watchersService.getFilteredWatcherInfo(woCode, hint);
        return ok(filteredWatcherInfo);
    }

    @GetMapping
    @RequestMapping("/workorders/{wo}/users/search")
    
    
    public ResponseEntity<?> completeFilteredByWOAccessSearch(@RequestParam("hint") String hint,
                                                      @PathVariable("wo") String woCode) {
        final List<WatcherInfo> filteredWatcherInfo = watchersService.getFilteredWatcherInfo(woCode, hint);
        return ok(filteredWatcherInfo);
    }
}