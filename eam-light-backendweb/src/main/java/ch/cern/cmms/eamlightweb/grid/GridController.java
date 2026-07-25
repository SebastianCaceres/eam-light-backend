package ch.cern.cmms.eamlightweb.grid;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult;
import ch.cern.eam.wshub.core.tools.InforException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/grids")
public class GridController extends EAMLightController {

	@Autowired
	private InforClient inforClient;
	@Autowired
	private AuthenticationTools authenticationTools;

	@PostMapping("/data")
	public ResponseEntity<?> readGridData(@RequestBody GridRequest gridRequest) {
		try {
			return ok(inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping("/export")
	public ResponseEntity<?> exportGridDataToCSV(@RequestBody GridRequest gridRequest) {
		String fileName = "dataGridExport.csv";
		String fileContent = "";
		try {
			// change the grid 'range'
			gridRequest.setCursorPosition(1);
			gridRequest.setRowCount(10000);
			// build the csv
			fileContent = inforClient.getGridsService().getGridCsvData(authenticationTools.getInforContext(), gridRequest);
		} catch (InforException exception) {
			return badRequest(exception);
		}

		return ResponseEntity.ok()
				.header("Content-Disposition", "attachment; filename=" + fileName)
				.body(fileContent);
	}

	@GetMapping("/{gridid}/metadata")
	public ResponseEntity<?> readGridMetaData(@PathVariable("gridid") String gridID, @RequestParam(value = "lang", required = false) String lang) {
		try {
			if (lang == null || (!"EN".equals(lang) && !"FR".equals(lang)))
				lang = "EN";
			return ok(inforClient.getGridsService().getGridMetadata(authenticationTools.getInforContext(), gridID, "LIST", lang));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@GetMapping("/{gridid}/dataspy")
	public ResponseEntity<?> readDataspyFields(@PathVariable("gridid") String gridCode, @RequestParam(value = "dataspyid", required = false) String ddSpyId,
			@RequestParam(value = "lang", required = false) String lang) {
		try {
			if (lang == null || (!"EN".equals(lang) && !"FR".equals(lang)))
				lang = "EN";
			return ok(inforClient.getGridsService().getDDspyFields(authenticationTools.getInforContext(), gridCode, "LIST", ddSpyId, lang));
		} catch (InforException e) {
			return badRequest(e);
		} catch(Exception e) {
			return serverError(e);
		}
	}

}
