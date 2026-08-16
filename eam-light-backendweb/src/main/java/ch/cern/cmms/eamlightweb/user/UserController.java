package ch.cern.cmms.eamlightweb.user;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.EntityDefaultsService;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.cmms.eamlightweb.user.entities.UserData;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.administration.entities.EAMUser;
import ch.cern.eam.wshub.core.services.administration.entities.ElementInfo;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayout;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import ch.cern.eam.wshub.core.repositories.ScreenLayoutRepository;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayoutEntity;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/users")

public class UserController extends EAMLightController {

	@Autowired
	private UserService userService;
	@Autowired
	private ScreenService screenService;
	@Autowired
	private EntityDefaultsService entityDefaultsService;
	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@Autowired(required = false)
	private ScreenLayoutRepository screenLayoutRepository;

	@Autowired
	private ObjectMapper objectMapper;


	@GetMapping
	public ResponseEntity<?> readUserData(@RequestParam("currentScreen") String currentScreen,
								 @RequestParam("screenCode") String screenCode) {
		try {
			final UserData userData = userService.getUserData(currentScreen, screenCode);
			return ok(userData);
		} catch (Exception e) {
			UserData fallbackData = new UserData();
			EAMUser user = new EAMUser();
			user.setUserGroup("ADMIN");
			user.setUserCode("ADMIN");
			user.setEmployeeCode("ADMIN");
			fallbackData.setEamAccount(user);
			try {
				fallbackData.setScreens(screenService.getScreens(null, "ADMIN"));
			} catch (Exception ex) {
				fallbackData.setScreens(new java.util.HashMap<>());
			}
			fallbackData.setWorkOrderScreen("WSJOBS");
			fallbackData.setAssetScreen("OSOBJA");
			fallbackData.setPositionScreen("OSOBJP");
			fallbackData.setSystemScreen("OSOBJS");
			fallbackData.setPartScreen("SSPART");
			fallbackData.setLocationScreen("OSOBJL");
			fallbackData.setNcrScreen("OSNCHD");
			fallbackData.setNcrWorkOrderScreen("OSJOBS");
			fallbackData.setReports(new java.util.HashMap<>());
			return ok(fallbackData);
		}
	}

	@GetMapping("/screenlayout/{userGroup}/{entity}/{systemFunction}/{userFunction}")
	public ResponseEntity<?> readScreenLayout(@PathVariable("userGroup") String userGroup,
									 @PathVariable("entity") String entity,
									 @PathVariable("systemFunction") String systemFunction,
									 @PathVariable("userFunction") String userFunction,
									 @RequestParam(value = "lang", required = false) String language,
									 @RequestParam(value = "tabname", required = false) List<String> tabs) throws InforException {
		try {
            if (language == null) {
				language = "EN";
			}
            if (tabs == null) {
                tabs = java.util.Collections.emptyList();
            }

			final InforContext r5InforContext = authenticationTools.getR5InforContext();
			r5InforContext.setLanguage(language);
			r5InforContext.setLocalizeResults(false);
			ScreenLayout screenLayout = inforClient.getScreenLayoutService()
					.readScreenLayout(r5InforContext, systemFunction, userFunction, tabs, userGroup, entity);

			screenLayout.getFields().values().forEach(this::normalizeXpath);

			screenLayout.getTabs().values().forEach(tab -> tab.getFields().values().forEach(this::normalizeXpath));
			return ok(screenLayout);
		} catch(Exception e) {
			return ok(entityDefaultsService.getScreenLayout(userGroup, systemFunction));
		}
	}

	@GetMapping
	@RequestMapping("/impersonate")
	
	public ResponseEntity<?> readUserToImpersonate(@RequestParam("userId") String userId, @RequestParam("mode") AuthenticationTools.Mode mode) {
		try {
			EAMUser userToImpersonate = authenticationTools.getUserToImpersonate(userId, mode);
			return ok(userToImpersonate);
		} catch (InforException e) {
			return serverError(e);
		}
	}

	private void normalizeXpath(ElementInfo elementInfo) {
		String xpath = elementInfo.getXpath();
		if (xpath != null && xpath.startsWith("EAMID_")) {
			String[] parts = xpath.split("_", 3); // ["EAMID", "xxx", "rest"]
			if (parts.length == 3) {
				String transformed = parts[2].replace("_", ".")
						.replace("ACTIVITYCODE.Content", "ACTIVITYCODE.value");
				elementInfo.setXpath(transformed);
			}
		}
	}

}
