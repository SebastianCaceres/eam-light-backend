package ch.cern.cmms.eamlightweb.index;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightejb.index.IndexEJB;
import ch.cern.cmms.eamlightejb.index.IndexGrids;
import ch.cern.cmms.eamlightejb.index.IndexResult;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;

import java.util.Arrays;
import java.util.List;

/**
 * Controller to handle the events related with the home screen of the
 * application
 *
 */
@Component
@RequestMapping("/index")

public class SearchController extends EAMLightController {

	@Autowired
	private IndexGrids indexGrids;
	@Autowired
	private AuthenticationTools authenticationTools;
	@Autowired
	private IndexEJB indexEJB;
	@Autowired
	private InforClient inforClient;

	@GetMapping
	public ResponseEntity<?> getSearchResults(@RequestParam("s") String searchKeyWord, @RequestParam("entityTypes") String entityTypes) {
		if (searchKeyWord != null) {
			searchKeyWord = searchKeyWord.trim();
		}

		List<IndexResult> indexResults;
		try {
			List<String> entityTypesList = Arrays.asList(entityTypes.split(","));
			if (inforClient.getTools().isDatabaseConnectionConfigured()) {
				indexResults = (entityTypes == null || entityTypes.trim().length() == 0) ?
					indexEJB.getIndexResultsFaster(searchKeyWord, authenticationTools.getInforContext().getCredentials().getUsername())
					: indexEJB.getIndexResultsFaster(searchKeyWord, authenticationTools.getInforContext().getCredentials().getUsername(), entityTypesList)
					;

			} else {
				indexResults = indexGrids.search(authenticationTools.getInforContext(), searchKeyWord, entityTypesList);
			}
			return ok(indexResults);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping
	@RequestMapping("/singleresult")
	
	public ResponseEntity<?> getIndexSingleResult(@RequestParam("s") String searchKeyword) {
		if (searchKeyword != null) {
			searchKeyword = searchKeyword.trim();
		}

		try {
			if (inforClient.getTools().isDatabaseConnectionConfigured()) {
				return ok(indexEJB.getIndexSingleResult(searchKeyword, authenticationTools.getInforContext().getCredentials().getUsername()));
			} else {
				return ok(indexGrids.searchSingleResult(authenticationTools.getInforContext(), searchKeyword));
			}
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
