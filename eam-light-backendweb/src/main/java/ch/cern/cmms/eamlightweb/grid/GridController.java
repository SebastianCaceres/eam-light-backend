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
			ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult result =
					inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest);
			java.util.Map<String, Object> map = new java.util.HashMap<>();
			map.put("gridCode", result != null ? result.getGridCode() : "");
			map.put("gridName", result != null ? result.getGridName() : "");
			map.put("cursorPosition", result != null ? result.getCursorPosition() : 1);
			map.put("records", result != null ? result.getRecords() : "0");
			java.util.List<?> gFields = (result != null && result.getGridFields() != null) ? java.util.Arrays.asList(result.getGridFields()) : new java.util.ArrayList<>();
			java.util.List<?> gRows = (result != null && result.getRows() != null) ? java.util.Arrays.asList(result.getRows()) : new java.util.ArrayList<>();
			map.put("gridFields", gFields);
			map.put("gridField", gFields);
			map.put("rows", gRows);
			map.put("row", gRows);
			map.put("DATARECORD", gRows);
			return ok(map);
		} catch (Exception e) {
			// Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
			String gridName = gridRequest != null ? gridRequest.getGridName() : "GRID";
			java.util.Map<String, Object> map = new java.util.HashMap<>();
			map.put("gridCode", gridName);
			map.put("gridName", gridName);
			map.put("cursorPosition", 1);
			map.put("records", "0");
			map.put("gridFields", new java.util.ArrayList<>());
			map.put("gridField", new java.util.ArrayList<>());
			map.put("gridDataspies", new java.util.ArrayList<>());
			map.put("rows", new java.util.ArrayList<>());
			map.put("row", new java.util.ArrayList<>());
			map.put("DATARECORD", new java.util.ArrayList<>());
			return ok(map);
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
		} catch (Exception exception) {
			fileContent = "";
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
		} catch (Exception e) {
			// Local standalone fallback
			ch.cern.eam.wshub.core.services.grids.entities.GridMetadataRequestResult metadata = new ch.cern.eam.wshub.core.services.grids.entities.GridMetadataRequestResult();
			metadata.setGridCode(gridID);
			metadata.setGridFields(new ch.cern.eam.wshub.core.services.grids.entities.GridField[0]);
			metadata.setGridDataspies(new ch.cern.eam.wshub.core.services.grids.entities.GridDataspy[0]);
			return ok(metadata);
		}
	}

	@GetMapping("/{gridid}/dataspy")
	public ResponseEntity<?> readDataspyFields(@PathVariable("gridid") String gridCode, @RequestParam(value = "dataspyid", required = false) String ddSpyId,
			@RequestParam(value = "lang", required = false) String lang) {
		try {
			if (lang == null || (!"EN".equals(lang) && !"FR".equals(lang)))
				lang = "EN";
			return ok(inforClient.getGridsService().getDDspyFields(authenticationTools.getInforContext(), gridCode, "LIST", ddSpyId, lang));
		} catch (Exception e) {
			java.util.Map<String, Object> map = new java.util.HashMap<>();
			map.put("gridField", new java.util.ArrayList<>());
			map.put("GRIDFIELD", new java.util.ArrayList<>());
			map.put("gridFields", new java.util.ArrayList<>());
			map.put("GRIDFIELDS", new java.util.ArrayList<>());
			map.put("gridDataspies", new java.util.ArrayList<>());
			map.put("gridFilters", new java.util.ArrayList<>());
			map.put("fields", new java.util.ArrayList<>());
			map.put("data", new java.util.ArrayList<>());
			return ok(map);
		}
	}

}
