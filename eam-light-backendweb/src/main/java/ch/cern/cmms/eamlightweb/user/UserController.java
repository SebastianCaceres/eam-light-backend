package ch.cern.cmms.eamlightweb.user;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
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

@RestController
@RequestMapping("/users")

public class UserController extends EAMLightController {

	@Autowired
	private UserService userService;
	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

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
			fallbackData.setScreens(new java.util.HashMap<>());
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
			// Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
			ScreenLayout fallbackLayout = new ScreenLayout();
			java.util.Map<String, ElementInfo> fieldMap = new java.util.HashMap<>();
			ElementInfo defaultInfo = new ElementInfo();
			defaultInfo.setAttribute("O");
			defaultInfo.setText("*");
			defaultInfo.setOnLookup("{\"lovName\":\"LVDEFAULT\",\"inputVars\":{},\"inputFields\":{},\"returnFields\":{}}");

			for (int i = 1; i <= 35; i++) {
				fieldMap.put("block_" + i, defaultInfo);
				fieldMap.put("BLOCK_" + i, defaultInfo);
			}
			String[] commonFields = {
				"status", "statusCode", "STATUSCODE", "type", "equipment", "department",
				"organization", "location", "parentlocation", "part", "ncr", "workorder", "asset",
				"parentasset", "position", "system", "primarysystem", "customFields", "userDefinedFields",
				"udfchar11", "udfchar12", "udfchar13", "udfchar14", "udfchar15",
				"udfchkbox01", "UDFCHKBOX01", "workordernum", "workOrdernum", "WORKORDERNUM",
				"partcode", "PARTCODE", "equipmentcode", "EQUIPMENTCODE", "locationcode", "LOCATIONCODE",
				"lotcode", "LOTCODE", "store", "STORE", "partdesc", "PARTDESC", "quantity", "QUANTITY",
				"uom", "UOM", "transactiontype", "TRANSACTIONTYPE", "partusage", "PARTUSAGE", "activity", "ACTIVITY",
				"storecode", "STORECODE", "assetid", "ASSETID", "bincode", "BINCODE",
				"transactionquantity", "TRANSACTIONQUANTITY"
			};
			for (String cf : commonFields) {
				fieldMap.put(cf, defaultInfo);
			}

			fallbackLayout.setFields(fieldMap);

			java.util.Map<String, ch.cern.eam.wshub.core.services.administration.entities.Tab> tabMap = new java.util.HashMap<>();
			ch.cern.eam.wshub.core.services.administration.entities.Tab defaultTab = new ch.cern.eam.wshub.core.services.administration.entities.Tab();
			System.out.println("=== TAB METHODS: " + java.util.Arrays.toString(ch.cern.eam.wshub.core.services.administration.entities.Tab.class.getMethods()));
			defaultTab.setTabAvailable(true);
			defaultTab.setAlwaysDisplayed(true);
			defaultTab.setTabDescription("Tab");
			defaultTab.setFields(fieldMap);
			tabMap.put("fields", defaultTab);

			String[] tabCodes = {
				"HDR", "EVT", "CMT", "CLO", "PAS", "UT1", "UT2", "UT5", "BIS", "EPA",
				"ACT", "ACK", "PAR", "REA", "MEC", "CWO", "DOC", "BOO", "ACO", "ESF",
				"OBS", "NCF", "NCT", "CLOSING_CODES", "HEADER", "WORKORDER", "COMMENTS",
				"PARTS", "DOCUMENTS", "ACTIVITIES", "BOOK_LABOR"
			};
			for (String tc : tabCodes) {
				tabMap.put(tc, defaultTab);
			}

			for (int i = 1; i <= 35; i++) {
				tabMap.put("TAB_" + i, defaultTab);
				tabMap.put("tab_" + i, defaultTab);
			}

			fallbackLayout.setTabs(tabMap);
			return ok(fallbackLayout);
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
