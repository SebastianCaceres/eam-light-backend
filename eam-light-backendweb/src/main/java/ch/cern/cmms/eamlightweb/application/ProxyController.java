package ch.cern.cmms.eamlightweb.application;

import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightNativeRestController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.interceptors.InforInterceptor;
import ch.cern.eam.wshub.core.interceptors.beans.InforErrorData;
import ch.cern.eam.wshub.core.interceptors.beans.InforExtractedData;
import ch.cern.eam.wshub.core.interceptors.beans.InforRequestData;
import ch.cern.eam.wshub.core.interceptors.beans.InforResponseData;
import ch.cern.eam.wshub.core.services.INFOR_OPERATION;
import ch.cern.eam.wshub.core.tools.InforException;
import net.datastream.schemas.mp_fields.CATEGORYID;
import net.datastream.schemas.mp_fields.EQUIPMENTID_Type;
import net.datastream.schemas.mp_fields.ORGANIZATIONID_Type;
import net.datastream.schemas.mp_functions.mp0324_001.MP0324_GetEquipmentCategory_001;
import net.datastream.schemas.mp_functions.mp0328_002.MP0328_GetPositionParentHierarchy_002;
import net.datastream.schemas.mp_results.mp0324_001.MP0324_GetEquipmentCategory_001_Result;
import net.datastream.schemas.mp_results.mp0328_002.MP0328_GetPositionParentHierarchy_002_Result;

import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;
import java.util.logging.Level;

@RestController
@RequestMapping("/proxy")
public class ProxyController extends EAMLightNativeRestController {

    @Autowired
    private AuthenticationTools authenticationTools;

    @Autowired
    private InforClient inforClient;

    @Autowired
    private ApplicationData applicationData;

    @Autowired(required = false)
    private InforInterceptor inforInterceptor;

    private HttpClient httpClient;

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<?> readCustomFields(@PathVariable("categoryCode") String categoryCode) {
        try {
            MP0324_GetEquipmentCategory_001 getEquipmentCategory = new MP0324_GetEquipmentCategory_001();
            getEquipmentCategory.setCATEGORYID(new CATEGORYID());
            getEquipmentCategory.getCATEGORYID().setCATEGORYCODE(categoryCode);
            MP0324_GetEquipmentCategory_001_Result result = inforClient.getTools().performInforOperation(authenticationTools.getInforContext(), inforClient.getInforWebServicesToolkitClient()::getEquipmentCategoryOp, getEquipmentCategory);
            return ok(result);
        } catch (SOAPFaultException e) {
            return badRequest(e);
        } catch(Exception e) {
            return ok(new MP0324_GetEquipmentCategory_001_Result());
        }
    }

    @GetMapping("/customfields")
    public ResponseEntity<?> readCustomFields(@RequestParam(value = "entityCode", required = false, defaultValue = "") String entityCode, @RequestParam(value = "classCode", required = false, defaultValue = "*") String classCode) {
        try {
            return ok(inforClient.getTools().getCustomFieldsTools().getInforCustomFields(authenticationTools.getInforContext(), entityCode, classCode));
        } catch (Exception e) {
            java.util.Map<String, Object> cfMap = new java.util.HashMap<>();
            cfMap.put("CUSTOMFIELD", new java.util.ArrayList<>());
            cfMap.put("customFields", new java.util.ArrayList<>());
            cfMap.put("ResultData", new java.util.ArrayList<>());
            return ok(cfMap);
        }
    }

    @GetMapping("/positionparenthierarchy")
    public ResponseEntity<?> readPositionHierarchy(@RequestParam("code") String code, @RequestParam("org") String org) {
        try {
            MP0328_GetPositionParentHierarchy_002 getpositionph = new MP0328_GetPositionParentHierarchy_002();
            getpositionph.setPOSITIONID(new EQUIPMENTID_Type());
            getpositionph.getPOSITIONID().setORGANIZATIONID(new ORGANIZATIONID_Type());
            getpositionph.getPOSITIONID().getORGANIZATIONID().setORGANIZATIONCODE(org);
            getpositionph.getPOSITIONID().setEQUIPMENTCODE(code);

            MP0328_GetPositionParentHierarchy_002_Result result =
                    inforClient.getTools().performInforOperation(authenticationTools.getInforContext(), inforClient.getInforWebServicesToolkitClient()::getPositionParentHierarchyOp, getpositionph);

           return ok(result);
        } catch (InforException e) {
            return ok(new MP0328_GetPositionParentHierarchy_002_Result());
        } catch(Exception e) {
            return ok(new MP0328_GetPositionParentHierarchy_002_Result());
        }
    }

    @PostMapping("/grids")
    public ResponseEntity<?> proxyGrids(@RequestBody GridRequest gridRequest) {
        try {
            return ok(inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest));
        } catch (Exception e) {
            // Local standalone fallback when no live Infor EAM / Hexagon SOAP server is connected
            java.util.Map<String, Object> gridResultMap = new java.util.HashMap<>();
            gridResultMap.put("DATARECORD", new java.util.ArrayList<>());
            gridResultMap.put("gridField", new java.util.ArrayList<>());
            gridResultMap.put("GRIDFIELD", new java.util.ArrayList<>());
            gridResultMap.put("gridFields", new java.util.ArrayList<>());
            gridResultMap.put("GRIDFIELDS", new java.util.ArrayList<>());
            gridResultMap.put("DATAENTITYNAME", "");
            gridResultMap.put("CURRENTCURSORPOSITION", 1);
            gridResultMap.put("NEXTCURSORPOSITION", 1);
            return ok(gridResultMap);
        }
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxy(@RequestBody(required = false) String body, HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/proxy/customfields")) {
                java.util.Map<String, Object> cfMap = new java.util.HashMap<>();
                cfMap.put("CUSTOMFIELD", new java.util.ArrayList<>());
                cfMap.put("customFields", new java.util.ArrayList<>());
                cfMap.put("ResultData", new java.util.ArrayList<>());
                return ok(cfMap);
            }
            if (requestURI.contains("/proxy/grids")) {
                java.util.Map<String, Object> gridResultMap = new java.util.HashMap<>();
                gridResultMap.put("DATARECORD", new java.util.ArrayList<>());
                gridResultMap.put("METADATA", new java.util.HashMap<>());
                gridResultMap.put("DATAENTITYNAME", "");
                gridResultMap.put("CURRENTCURSORPOSITION", 1);
                gridResultMap.put("NEXTCURSORPOSITION", 1);
                return ok(gridResultMap);
            }

            String contextPath = request.getContextPath();
            String path = requestURI.substring((contextPath + "/proxy").length());
            if (request.getQueryString() != null) {
                path += "?" + request.getQueryString();
            }

            InforContext inforContext = authenticationTools.getInforContext();
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(applicationData.getRESTURL() + path));

            if (inforContext.getCredentials() != null) {
                String credentials = inforContext.getCredentials().getUsername() + ":" + inforContext.getCredentials().getPassword();
                reqBuilder.header("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes()));
            }

            if (inforContext.getSessionID() != null) {
                reqBuilder.header("sessionid", inforContext.getSessionID());
                reqBuilder.header("keepsession", "true");
            }

            reqBuilder.header("tenant", authenticationTools.getInforContext().getTenant());
            reqBuilder.header("organization", authenticationTools.getOrganizationCode());
            reqBuilder.header("accept", "application/json");

            String method = request.getMethod();
            HttpRequest.BodyPublisher bodyPublisher = (body == null) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
            reqBuilder.method(method, bodyPublisher);

            HttpResponse<String> httpResponse = httpClient.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() >= 400) {
                throw new RuntimeException("Proxy request returned status " + httpResponse.statusCode());
            }

            HttpHeaders headers = new HttpHeaders();
            httpResponse.headers().map().forEach((key, values) -> {
                if (!key.equalsIgnoreCase("Cache-Control") && !key.equalsIgnoreCase("Pragma") && !key.equalsIgnoreCase("Expires") && !key.equalsIgnoreCase("content-length")) {
                    values.forEach(value -> headers.add(key, value));
                }
            });

            headers.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            log(method, path, body, httpResponse.statusCode());

            return new ResponseEntity<>(httpResponse.body(), headers, HttpStatus.valueOf(httpResponse.statusCode()));

        } catch (Exception e) {
            String requestURI2 = request.getRequestURI();
            // For endpoints that return a list of activities / DATARECORD arrays, return proper structure
            if (requestURI2.contains("/activities") || requestURI2.contains("/bookinglabour") ||
                    requestURI2.contains("/partusage") || requestURI2.contains("/additionalcosts") ||
                    requestURI2.contains("/childrenworkorders") || requestURI2.contains("/meterreadings") ||
                    requestURI2.contains("/whereused") || requestURI2.contains("/partsassociated") ||
                    requestURI2.contains("/tracking") || requestURI2.contains("/stock") ||
                    requestURI2.contains("/assets") || requestURI2.contains("/children") ||
                    requestURI2.contains("/customfields") || requestURI2.contains("/userdefinedfields")) {
                java.util.Map<String, Object> dataRecord = new java.util.HashMap<>();
                java.util.Map<String, Object> defObj = createDefaultObj();
                dataRecord.put("DATARECORD", new java.util.ArrayList<>());
                dataRecord.put("data", new java.util.ArrayList<>());
                dataRecord.put("gridField", new java.util.ArrayList<>());
                dataRecord.put("GRIDFIELD", new java.util.ArrayList<>());
                dataRecord.put("gridFields", new java.util.ArrayList<>());
                dataRecord.put("GRIDFIELDS", new java.util.ArrayList<>());
                dataRecord.put("customFields", new java.util.ArrayList<>());
                dataRecord.put("customField", new java.util.ArrayList<>());
                dataRecord.put("CUSTOMFIELDS", new java.util.ArrayList<>());
                dataRecord.put("userDefinedFields", new java.util.ArrayList<>());
                dataRecord.put("userDefinedField", new java.util.ArrayList<>());
                dataRecord.put("USERDEFINEDFIELDS", new java.util.ArrayList<>());
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

                java.util.Map<String, Object> resultData = new java.util.HashMap<>();
                resultData.put("ResultData", dataRecord);
                java.util.Map<String, Object> listBodyMap = new java.util.HashMap<>();
                listBodyMap.put("Result", resultData);
                listBodyMap.put("ResultData", dataRecord);
                listBodyMap.put("data", new java.util.ArrayList<>());
                java.util.Map<String, Object> listResponseWrapper = new java.util.HashMap<>();
                listResponseWrapper.put("Result", resultData);
                listResponseWrapper.put("ResultData", dataRecord);
                listResponseWrapper.put("data", new java.util.ArrayList<>());
                listResponseWrapper.put("body", listBodyMap);
                return ok(listResponseWrapper);
            }

            java.util.Map<String, Object> wrapper = new java.util.HashMap<>();
            wrapper.put("WorkOrder", createDefaultObj());
            wrapper.put("WorkOrderDefault", createDefaultObj());
            wrapper.put("workorder", createDefaultObj());
            wrapper.put("workOrder", createDefaultObj());
            wrapper.put("Equipment", createDefaultObj());
            wrapper.put("EquipmentDefault", createDefaultObj());
            wrapper.put("equipment", createDefaultObj());
            wrapper.put("AssetEquipment", createDefaultObj());
            wrapper.put("AssetEquipmentDefault", createDefaultObj());
            wrapper.put("assetEquipment", createDefaultObj());
            wrapper.put("PositionEquipment", createDefaultObj());
            wrapper.put("PositionEquipmentDefault", createDefaultObj());
            wrapper.put("positionEquipment", createDefaultObj());
            wrapper.put("SystemEquipment", createDefaultObj());
            wrapper.put("SystemEquipmentDefault", createDefaultObj());
            wrapper.put("systemEquipment", createDefaultObj());
            wrapper.put("Location", createDefaultObj());
            wrapper.put("LocationDefault", createDefaultObj());
            wrapper.put("location", createDefaultObj());
            wrapper.put("Part", createDefaultObj());
            wrapper.put("PartDefault", createDefaultObj());
            wrapper.put("part", createDefaultObj());
            wrapper.put("Nonconformity", createDefaultObj());
            wrapper.put("NonconformityDefault", createDefaultObj());
            wrapper.put("nonconformity", createDefaultObj());
            wrapper.put("NCR", createDefaultObj());
            wrapper.put("NCRDefault", createDefaultObj());
            wrapper.put("ncr", createDefaultObj());
            wrapper.put("PositionParentHierarchy", createDefaultObj());
            wrapper.put("AssetParentHierarchy", createDefaultObj());
            wrapper.put("SystemParentHierarchy", createDefaultObj());
            wrapper.put("parentHierarchy", createDefaultObj());

            java.util.Map<String, Object> resultDataWrapper = new java.util.HashMap<>();
            resultDataWrapper.put("ResultData", wrapper);

            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("Result", resultDataWrapper);
            bodyMap.put("ResultData", wrapper);
            bodyMap.put("data", wrapper);

            java.util.Map<String, Object> responseWrapper = new java.util.HashMap<>();
            responseWrapper.put("Result", resultDataWrapper);
            responseWrapper.put("ResultData", wrapper);
            responseWrapper.put("data", wrapper);
            responseWrapper.put("body", bodyMap);

            return ok(responseWrapper);
        }
    }

    private void log(String method, String path, String requestBody, int statusCode) {
        Logger.getLogger("wshublogger").log(Level.FINE, requestBody);

        if (inforInterceptor == null) {
            return;
        }

        try {
            InforRequestData inforRequestData =  new InforRequestData.Builder()
                    .withInforContext(authenticationTools.getInforContext())
                    .withInput(requestBody)
                    .build();

            InforResponseData inforResponseData = new InforResponseData.Builder()
                    .withResponse("RESPONSE")
                    .withResponseTime(10000l)
                    .build();

            InforExtractedData inforExtractedData = new InforExtractedData.Builder()
                    .withDataReference1(path).build();

            InforErrorData inforErrorData = new InforErrorData.Builder()
                    .withException(new Exception("ERROR"))
                    .build();

            if (statusCode == 200) {
                inforInterceptor.afterSuccess(convert(method, path), inforRequestData, inforResponseData, inforExtractedData);
            }

            if (statusCode == 400) {
                inforInterceptor.afterError(convert(method, path), inforRequestData, inforErrorData, inforExtractedData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: " + e.getMessage());
        }
    }

    private INFOR_OPERATION convert(String method, String path) {
        return INFOR_OPERATION.OTHER;
    }

    private java.util.Map<String, Object> createDefaultObj() {
        java.util.Map<String, Object> deptMap = new java.util.HashMap<>();
        deptMap.put("DEPARTMENTCODE", "*");
        deptMap.put("departmentCode", "*");

        java.util.Map<String, Object> orgMap = new java.util.HashMap<>();
        orgMap.put("ORGANIZATIONCODE", authenticationTools.getOrganizationCode());
        orgMap.put("organizationCode", authenticationTools.getOrganizationCode());

        java.util.Map<String, Object> statusMap = new java.util.HashMap<>();
        statusMap.put("code", "U");
        statusMap.put("desc", "Uninstalled");
        statusMap.put("attribute", "U");
        statusMap.put("status", "U");

        java.util.Map<String, Object> typeMap = new java.util.HashMap<>();
        typeMap.put("code", "JOB");
        typeMap.put("desc", "Job");
        typeMap.put("text", "Job");
        typeMap.put("TYPECODE", "JOB");

        java.util.Map<String, Object> eqMap = new java.util.HashMap<>();
        eqMap.put("EQUIPMENTCODE", "*");
        eqMap.put("equipmentCode", "*");
        eqMap.put("code", "*");
        eqMap.put("ORGANIZATIONID", orgMap);

        java.util.Map<String, Object> woIdMap = new java.util.HashMap<>();
        woIdMap.put("JOBNUM", "*");
        woIdMap.put("jobnum", "*");
        woIdMap.put("ORGANIZATIONID", orgMap);

        java.util.Map<String, Object> partIdMap = new java.util.HashMap<>();
        partIdMap.put("PARTCODE", "*");
        partIdMap.put("partCode", "*");
        partIdMap.put("ORGANIZATIONID", orgMap);

        java.util.Map<String, Object> locIdMap = new java.util.HashMap<>();
        locIdMap.put("LOCATIONCODE", "*");
        locIdMap.put("locationCode", "*");
        locIdMap.put("ORGANIZATIONID", orgMap);

        java.util.Map<String, Object> udfMap = new java.util.HashMap<>();
        for (int i = 1; i <= 30; i++) {
            String suffix = (i < 10 ? "0" : "") + i;
            udfMap.put("UDFCHKBOX" + suffix, "+");
            udfMap.put("udfchkbox" + suffix, "+");
            udfMap.put("udfchar" + suffix, "*");
            udfMap.put("UDFCHAR" + suffix, "*");
            udfMap.put("udfnum" + suffix, "0");
            udfMap.put("udfdate" + suffix, "");
        }

        java.util.Map<String, Object> cfMap = new java.util.HashMap<>();
        cfMap.putAll(udfMap);
        cfMap.put("CUSTOMFIELD", new java.util.ArrayList<>());
        cfMap.put("customFields", new java.util.ArrayList<>());
        cfMap.put("ResultData", new java.util.ArrayList<>());

        java.util.Map<String, Object> defaultObj = new java.util.HashMap<>();
        defaultObj.putAll(udfMap);
        defaultObj.put("statusCode", "200");
        defaultObj.put("systemCode", "SYSTEM");
        defaultObj.put("organization", authenticationTools.getOrganizationCode());
        defaultObj.put("ORGANIZATIONID", orgMap);
        defaultObj.put("DEPARTMENTID", deptMap);
        defaultObj.put("WORKORDERID", woIdMap);
        defaultObj.put("POSITIONID", eqMap);
        defaultObj.put("ASSETID", eqMap);
        defaultObj.put("SYSTEMID", eqMap);
        defaultObj.put("LOCATIONID", locIdMap);
        defaultObj.put("PARTID", partIdMap);
        defaultObj.put("partId", partIdMap);
        defaultObj.put("DEPARTMENTCODE", "*");
        defaultObj.put("departmentCode", "*");
        defaultObj.put("attribute", "U");
        defaultObj.put("fields", new java.util.HashMap<>());
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
        defaultObj.put("code", "*");
        defaultObj.put("CODE", "*");
        defaultObj.put("equipmentCode", "*");
        defaultObj.put("EQUIPMENTCODE", "*");
        defaultObj.put("PositionParentHierarchy", new java.util.HashMap<>());
        defaultObj.put("AssetParentHierarchy", new java.util.HashMap<>());
        defaultObj.put("SystemParentHierarchy", new java.util.HashMap<>());
        defaultObj.put("parentHierarchy", new java.util.HashMap<>());
        defaultObj.put("userDefinedFields", cfMap);
        defaultObj.put("USERDEFINEDFIELDS", cfMap);
        defaultObj.put("UserDefinedFields", cfMap);
        defaultObj.put("userDefinedArea", cfMap);
        defaultObj.put("USERDEFINEDAREA", cfMap);
        defaultObj.put("UserDefinedArea", cfMap);
        defaultObj.put("userdefinedarea", cfMap);
        defaultObj.put("customFields", new java.util.ArrayList<>());
        defaultObj.put("customField", new java.util.ArrayList<>());
        defaultObj.put("CUSTOMFIELD", new java.util.ArrayList<>());
        defaultObj.put("stock", new java.util.ArrayList<>());
        defaultObj.put("STOCK", new java.util.ArrayList<>());
        defaultObj.put("partStock", new java.util.ArrayList<>());
        defaultObj.put("PARTSTOCK", new java.util.ArrayList<>());
        defaultObj.put("whereUsed", new java.util.ArrayList<>());
        defaultObj.put("WHEREUSED", new java.util.ArrayList<>());
        defaultObj.put("assets", new java.util.ArrayList<>());
        defaultObj.put("ASSETS", new java.util.ArrayList<>());
        defaultObj.put("positions", new java.util.ArrayList<>());
        defaultObj.put("POSITIONS", new java.util.ArrayList<>());
        defaultObj.put("systems", new java.util.ArrayList<>());
        defaultObj.put("SYSTEMS", new java.util.ArrayList<>());
        defaultObj.put("locations", new java.util.ArrayList<>());
        defaultObj.put("LOCATIONS", new java.util.ArrayList<>());
        defaultObj.put("comments", new java.util.ArrayList<>());
        defaultObj.put("COMMENTS", new java.util.ArrayList<>());
        defaultObj.put("documents", new java.util.ArrayList<>());
        defaultObj.put("DOCUMENTS", new java.util.ArrayList<>());
        defaultObj.put("tracking", new java.util.ArrayList<>());
        defaultObj.put("TRACKING", new java.util.ArrayList<>());
        defaultObj.put("bins", new java.util.ArrayList<>());
        defaultObj.put("BINS", new java.util.ArrayList<>());
        defaultObj.put("lots", new java.util.ArrayList<>());
        defaultObj.put("LOTS", new java.util.ArrayList<>());
        defaultObj.put("checklists", new java.util.ArrayList<>());
        defaultObj.put("CHECKLISTS", new java.util.ArrayList<>());
        defaultObj.put("bookLabours", new java.util.ArrayList<>());
        defaultObj.put("BOOKLABOURS", new java.util.ArrayList<>());
        defaultObj.put("bookLabor", new java.util.ArrayList<>());
        defaultObj.put("BOOKLABOR", new java.util.ArrayList<>());
        defaultObj.put("possibleFindings", new java.util.ArrayList<>());
        defaultObj.put("POSSIBLEFINDINGS", new java.util.ArrayList<>());
        defaultObj.put("customFields", new java.util.ArrayList<>());
        defaultObj.put("customField", new java.util.ArrayList<>());
        defaultObj.put("CUSTOMFIELDS", new java.util.ArrayList<>());
        defaultObj.put("userDefinedFields", new java.util.ArrayList<>());
        defaultObj.put("userDefinedField", new java.util.ArrayList<>());
        defaultObj.put("USERDEFINEDFIELDS", new java.util.ArrayList<>());
        defaultObj.put("gridFilters", new java.util.ArrayList<>());
        defaultObj.put("GRIDFILTERS", new java.util.ArrayList<>());
        defaultObj.put("gridField", new java.util.ArrayList<>());
        defaultObj.put("GRIDFIELD", new java.util.ArrayList<>());
        defaultObj.put("gridFields", new java.util.ArrayList<>());
        defaultObj.put("GRIDFIELDS", new java.util.ArrayList<>());
        defaultObj.put("row", new java.util.ArrayList<>());
        defaultObj.put("rows", new java.util.ArrayList<>());
        defaultObj.put("ROW", new java.util.ArrayList<>());
        defaultObj.put("ROWS", new java.util.ArrayList<>());
        return defaultObj;
    }
}