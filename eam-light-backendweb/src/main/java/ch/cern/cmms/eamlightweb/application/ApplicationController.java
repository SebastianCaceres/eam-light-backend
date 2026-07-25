package ch.cern.cmms.eamlightweb.application;

import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.cmms.eamlightweb.cache.CacheManager;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.cmms.plugins.LDAPPlugin;
import ch.cern.cmms.plugins.SharedPlugin;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/application")

public class ApplicationController extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private ApplicationData applicationData;
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private ApplicationService applicationService;
	@Autowired
	private SharedPlugin sharedPlugin;
	@Autowired
	private LDAPPlugin ldapPlugin;
    @Autowired
    private AuthenticationTools authenticationTools;

	@GetMapping
	@RequestMapping("/hello")
	
	
	public ResponseEntity<?> sayHello() {
		return ok(sharedPlugin.sayHello() + " (EAMLIGHT_INFOR_WS_URL=" + applicationData.getInforWSURL() + ")");
	}

	@GetMapping
	@RequestMapping("/applicationdata")
	
	
	public ResponseEntity<?> readApplicationData() {
		try {
			final InforContext inforContext = authenticationTools.getInforContext();
			return ok(applicationService.getParams(inforContext.getTenant()));
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/refreshCache")
	
	
	public ResponseEntity<?> cleanCache() {
		cacheManager.clearAllCaches();
		return ok("EAM Light cache has been successfully refreshed.");
	}

	@GetMapping
	@RequestMapping("/version")
	
	public ResponseEntity<?> readVersion() {
		String version = applicationData.getVersion();
		return ok(version);
	}

}