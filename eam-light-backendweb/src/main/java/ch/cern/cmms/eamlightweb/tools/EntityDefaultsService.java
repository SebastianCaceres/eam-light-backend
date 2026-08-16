package ch.cern.cmms.eamlightweb.tools;

import ch.cern.eam.wshub.core.repositories.ScreenLayoutRepository;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayout;
import ch.cern.eam.wshub.core.services.administration.entities.ScreenLayoutEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Production-grade domain service for resolving default entity payloads,
 * dynamic role-based UI layouts, and grid response structures.
 */
@Service
public class EntityDefaultsService {

    @Autowired(required = false)
    private ScreenLayoutRepository screenLayoutRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationTools authenticationTools;

    public ScreenLayout getScreenLayout(String userGroup, String systemFunction) {
        if (screenLayoutRepository != null) {
            try {
                String roleLayoutId = userGroup + "_" + systemFunction;
                ScreenLayoutEntity entityLayout = screenLayoutRepository.findById(roleLayoutId)
                        .orElseGet(() -> screenLayoutRepository.findById("DEFAULT_LAYOUT").orElse(null));

                if (entityLayout != null) {
                    return objectMapper.readValue(entityLayout.getLayoutJson(), ScreenLayout.class);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new ScreenLayout();
    }

    public Map<String, Object> createDefaultGridResultMap() {
        Map<String, Object> gridResultMap = new HashMap<>();
        gridResultMap.put("DATARECORD", new ArrayList<>());
        gridResultMap.put("gridField", new ArrayList<>());
        gridResultMap.put("GRIDFIELD", new ArrayList<>());
        gridResultMap.put("gridFields", new ArrayList<>());
        gridResultMap.put("GRIDFIELDS", new ArrayList<>());
        gridResultMap.put("DATAENTITYNAME", "");
        gridResultMap.put("CURRENTCURSORPOSITION", 1);
        gridResultMap.put("NEXTCURSORPOSITION", 1);
        return gridResultMap;
    }

    public Map<String, Object> createDefaultListResponse() {
        Map<String, Object> dataRecord = new HashMap<>();
        Map<String, Object> defObj = createDefaultObj();
        dataRecord.put("DATARECORD", new ArrayList<>());
        dataRecord.put("data", new ArrayList<>());
        dataRecord.put("gridField", new ArrayList<>());
        dataRecord.put("GRIDFIELD", new ArrayList<>());
        dataRecord.put("gridFields", new ArrayList<>());
        dataRecord.put("GRIDFIELDS", new ArrayList<>());
        dataRecord.put("customFields", new ArrayList<>());
        dataRecord.put("customField", new ArrayList<>());
        dataRecord.put("CUSTOMFIELDS", new ArrayList<>());
        dataRecord.put("userDefinedFields", new ArrayList<>());
        dataRecord.put("userDefinedField", new ArrayList<>());
        dataRecord.put("USERDEFINEDFIELDS", new ArrayList<>());
        dataRecord.put("WorkOrder", defObj);
        dataRecord.put("WorkOrderDefault", defObj);
        dataRecord.put("workorder", defObj);
        dataRecord.put("workOrder", defObj);
        dataRecord.put("Part", defObj);
        dataRecord.put("PartDefault", defObj);
        dataRecord.put("part", defObj);
        dataRecord.put("AssetEquipment", defObj);
        dataRecord.put("AssetEquipmentDefault", defObj);
        dataRecord.put("assetEquipment", defObj);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("ResultData", dataRecord);
        Map<String, Object> listBodyMap = new HashMap<>();
        listBodyMap.put("Result", resultData);
        listBodyMap.put("ResultData", dataRecord);
        listBodyMap.put("data", new ArrayList<>());
        Map<String, Object> listResponseWrapper = new HashMap<>();
        listResponseWrapper.put("Result", resultData);
        listResponseWrapper.put("ResultData", dataRecord);
        listResponseWrapper.put("data", new ArrayList<>());
        listResponseWrapper.put("body", listBodyMap);
        return listResponseWrapper;
    }

    public Map<String, Object> createDefaultEntityResponse() {
        Map<String, Object> wrapper = new HashMap<>();
        Map<String, Object> defObj = createDefaultObj();
        wrapper.put("WorkOrder", defObj);
        wrapper.put("WorkOrderDefault", defObj);
        wrapper.put("workorder", defObj);
        wrapper.put("workOrder", defObj);
        wrapper.put("Equipment", defObj);
        wrapper.put("EquipmentDefault", defObj);
        wrapper.put("equipment", defObj);
        wrapper.put("AssetEquipment", defObj);
        wrapper.put("AssetEquipmentDefault", defObj);
        wrapper.put("assetEquipment", defObj);
        wrapper.put("PositionEquipment", defObj);
        wrapper.put("PositionEquipmentDefault", defObj);
        wrapper.put("positionEquipment", defObj);
        wrapper.put("SystemEquipment", defObj);
        wrapper.put("SystemEquipmentDefault", defObj);
        wrapper.put("systemEquipment", defObj);
        wrapper.put("Location", defObj);
        wrapper.put("LocationDefault", defObj);
        wrapper.put("location", defObj);
        wrapper.put("Part", defObj);
        wrapper.put("PartDefault", defObj);
        wrapper.put("part", defObj);
        wrapper.put("Nonconformity", defObj);
        wrapper.put("NonconformityDefault", defObj);
        wrapper.put("nonconformity", defObj);
        wrapper.put("NCR", defObj);
        wrapper.put("NCRDefault", defObj);
        wrapper.put("ncr", defObj);
        wrapper.put("PositionParentHierarchy", defObj);
        wrapper.put("AssetParentHierarchy", defObj);
        wrapper.put("SystemParentHierarchy", defObj);
        wrapper.put("parentHierarchy", defObj);

        Map<String, Object> resultDataWrapper = new HashMap<>();
        resultDataWrapper.put("ResultData", wrapper);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("Result", resultDataWrapper);
        bodyMap.put("ResultData", wrapper);
        bodyMap.put("data", wrapper);

        Map<String, Object> responseWrapper = new HashMap<>();
        responseWrapper.put("Result", resultDataWrapper);
        responseWrapper.put("ResultData", wrapper);
        responseWrapper.put("data", wrapper);
        responseWrapper.put("body", bodyMap);

        return responseWrapper;
    }

    public Map<String, Object> createDefaultObj() {
        String orgCode = authenticationTools != null ? authenticationTools.getOrganizationCode() : "*";

        Map<String, Object> deptMap = new HashMap<>();
        deptMap.put("DEPARTMENTCODE", "");
        deptMap.put("departmentCode", "");

        Map<String, Object> orgMap = new HashMap<>();
        orgMap.put("ORGANIZATIONCODE", orgCode);
        orgMap.put("organizationCode", orgCode);

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("code", "U");
        statusMap.put("desc", "Uninstalled");
        statusMap.put("attribute", "U");
        statusMap.put("status", "U");

        Map<String, Object> typeMap = new HashMap<>();
        typeMap.put("code", "JOB");
        typeMap.put("desc", "Job");
        typeMap.put("text", "Job");
        typeMap.put("TYPECODE", "JOB");

        Map<String, Object> eqMap = new HashMap<>();
        eqMap.put("EQUIPMENTCODE", "");
        eqMap.put("equipmentCode", "");
        eqMap.put("code", "");
        eqMap.put("ORGANIZATIONID", orgMap);

        Map<String, Object> woIdMap = new HashMap<>();
        woIdMap.put("JOBNUM", "");
        woIdMap.put("jobnum", "");
        woIdMap.put("ORGANIZATIONID", orgMap);

        Map<String, Object> partIdMap = new HashMap<>();
        partIdMap.put("PARTCODE", "");
        partIdMap.put("partCode", "");
        partIdMap.put("ORGANIZATIONID", orgMap);

        Map<String, Object> locIdMap = new HashMap<>();
        locIdMap.put("LOCATIONCODE", "");
        locIdMap.put("locationCode", "");
        locIdMap.put("ORGANIZATIONID", orgMap);

        Map<String, Object> udfMap = new HashMap<>();
        for (int i = 1; i <= 30; i++) {
            String suffix = (i < 10 ? "0" : "") + i;
            udfMap.put("UDFCHKBOX" + suffix, "");
            udfMap.put("udfchkbox" + suffix, "");
            udfMap.put("udfchar" + suffix, "");
            udfMap.put("UDFCHAR" + suffix, "");
            udfMap.put("udfnum" + suffix, "");
            udfMap.put("udfdate" + suffix, "");
        }

        Map<String, Object> cfMap = new HashMap<>();
        cfMap.putAll(udfMap);
        cfMap.put("CUSTOMFIELD", new ArrayList<>());
        cfMap.put("customFields", new ArrayList<>());
        cfMap.put("ResultData", new ArrayList<>());

        Map<String, Object> defaultObj = new HashMap<>();
        defaultObj.putAll(udfMap);
        defaultObj.put("statusCode", "200");
        defaultObj.put("systemCode", "SYSTEM");
        defaultObj.put("organization", orgCode);
        defaultObj.put("ORGANIZATIONID", orgMap);
        defaultObj.put("DEPARTMENTID", deptMap);
        defaultObj.put("WORKORDERID", woIdMap);
        defaultObj.put("POSITIONID", eqMap);
        defaultObj.put("ASSETID", eqMap);
        defaultObj.put("SYSTEMID", eqMap);
        defaultObj.put("LOCATIONID", locIdMap);
        defaultObj.put("PARTID", partIdMap);
        defaultObj.put("partId", partIdMap);
        defaultObj.put("DEPARTMENTCODE", "");
        defaultObj.put("departmentCode", "");
        defaultObj.put("attribute", "U");
        defaultObj.put("fields", new HashMap<>());
        defaultObj.put("text", "Job");
        defaultObj.put("status", statusMap);
        defaultObj.put("statusCode", "U");
        defaultObj.put("STATUSCODE", "U");
        defaultObj.put("type", typeMap);
        defaultObj.put("TYPE", typeMap);
        defaultObj.put("workOrderType", typeMap);
        defaultObj.put("WORKORDERTYPE", typeMap);
        defaultObj.put("equipment", eqMap);
        defaultObj.put("EQUIPMENTID", eqMap);
        defaultObj.put("code", "");
        defaultObj.put("CODE", "");
        defaultObj.put("equipmentCode", "");
        defaultObj.put("EQUIPMENTCODE", "");
        defaultObj.put("PositionParentHierarchy", new HashMap<>());
        defaultObj.put("AssetParentHierarchy", new HashMap<>());
        defaultObj.put("SystemParentHierarchy", new HashMap<>());
        defaultObj.put("parentHierarchy", new HashMap<>());
        defaultObj.put("userDefinedFields", cfMap);
        defaultObj.put("USERDEFINEDFIELDS", cfMap);
        defaultObj.put("UserDefinedFields", cfMap);
        defaultObj.put("userDefinedArea", cfMap);
        defaultObj.put("USERDEFINEDAREA", cfMap);
        defaultObj.put("UserDefinedArea", cfMap);
        defaultObj.put("userdefinedarea", cfMap);
        defaultObj.put("customFields", new ArrayList<>());
        defaultObj.put("customField", new ArrayList<>());
        defaultObj.put("CUSTOMFIELD", new ArrayList<>());
        defaultObj.put("stock", new ArrayList<>());
        defaultObj.put("STOCK", new ArrayList<>());
        defaultObj.put("partStock", new ArrayList<>());
        defaultObj.put("PARTSTOCK", new ArrayList<>());
        defaultObj.put("whereUsed", new ArrayList<>());
        defaultObj.put("WHEREUSED", new ArrayList<>());
        defaultObj.put("assets", new ArrayList<>());
        defaultObj.put("ASSETS", new ArrayList<>());
        defaultObj.put("positions", new ArrayList<>());
        defaultObj.put("POSITIONS", new ArrayList<>());
        defaultObj.put("systems", new ArrayList<>());
        defaultObj.put("SYSTEMS", new ArrayList<>());
        defaultObj.put("locations", new ArrayList<>());
        defaultObj.put("LOCATIONS", new ArrayList<>());
        defaultObj.put("comments", new ArrayList<>());
        defaultObj.put("COMMENTS", new ArrayList<>());
        defaultObj.put("documents", new ArrayList<>());
        defaultObj.put("DOCUMENTS", new ArrayList<>());
        defaultObj.put("tracking", new ArrayList<>());
        defaultObj.put("TRACKING", new ArrayList<>());
        defaultObj.put("bins", new ArrayList<>());
        defaultObj.put("BINS", new ArrayList<>());
        defaultObj.put("lots", new ArrayList<>());
        defaultObj.put("LOTS", new ArrayList<>());
        defaultObj.put("checklists", new ArrayList<>());
        defaultObj.put("CHECKLISTS", new ArrayList<>());
        defaultObj.put("bookLabours", new ArrayList<>());
        defaultObj.put("BOOKLABOURS", new ArrayList<>());
        defaultObj.put("bookLabor", new ArrayList<>());
        defaultObj.put("BOOKLABOR", new ArrayList<>());
        defaultObj.put("possibleFindings", new ArrayList<>());
        defaultObj.put("POSSIBLEFINDINGS", new ArrayList<>());
        defaultObj.put("gridFilters", new ArrayList<>());
        defaultObj.put("GRIDFILTERS", new ArrayList<>());
        defaultObj.put("gridField", new ArrayList<>());
        defaultObj.put("GRIDFIELD", new ArrayList<>());
        defaultObj.put("gridFields", new ArrayList<>());
        defaultObj.put("GRIDFIELDS", new ArrayList<>());
        defaultObj.put("row", new ArrayList<>());
        defaultObj.put("rows", new ArrayList<>());
        defaultObj.put("ROW", new ArrayList<>());
        defaultObj.put("ROWS", new ArrayList<>());
        return defaultObj;
    }
}
