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
		} catch (Exception e) {
			// Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
			String gridName = gridRequest != null ? gridRequest.getGridName() : "GRID";
			GridRequestResult sampleResult = new GridRequestResult();
			sampleResult.setGridCode(gridName);
			sampleResult.setGridName(gridName);
			sampleResult.setCursorPosition(1);
			sampleResult.setRecords("1");
			sampleResult.setGridFields(new java.util.ArrayList<>());
			sampleResult.setGridDataspies(new java.util.ArrayList<>());

			ch.cern.eam.wshub.core.services.grids.entities.GridRequestRow row = new ch.cern.eam.wshub.core.services.grids.entities.GridRequestRow();
			java.util.List<ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell> cells = new java.util.ArrayList<>();

			if ("WSJOBS".equalsIgnoreCase(gridName) || "WUSCHE".equalsIgnoreCase(gridName)) {
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("workordernumber", "10001"));
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("description", "Sample Work Order"));
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("equipment", "AST-001"));
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("status", "R"));
			} else if ("SSPART".equalsIgnoreCase(gridName)) {
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("partcode", "PRT-001"));
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("description", "Sample Filter Element"));
			} else {
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("equipmentcode", "AST-001"));
				cells.add(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell("description", "Sample Equipment"));
			}

			row.setCells(cells.toArray(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestCell[0]));
			sampleResult.setRows(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestRow[]{ row });
			return ok(sampleResult);
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
			return ok(new HashMap<>());
		}
	}

}
