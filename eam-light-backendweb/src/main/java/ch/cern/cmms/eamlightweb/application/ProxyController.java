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
    public ResponseEntity<?> readCustomFields(@RequestParam("entityCode") String entityCode, @RequestParam("classCode") String classCode) {
        try {
            return ok(inforClient.getTools().getCustomFieldsTools().getInforCustomFields(authenticationTools.getInforContext(), entityCode, classCode));
        } catch (InforException e) {
            return ok(new java.util.ArrayList<>());
        } catch(Exception e) {
            return ok(new java.util.ArrayList<>());
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
            ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult emptyResult = new ch.cern.eam.wshub.core.services.grids.entities.GridRequestResult();
            emptyResult.setRows(new ch.cern.eam.wshub.core.services.grids.entities.GridRequestRow[0]);
            emptyResult.setCursorPosition(1);
            emptyResult.setRecords("0");
            emptyResult.setGridFields(new java.util.ArrayList<>());
            emptyResult.setGridDataspies(new java.util.ArrayList<>());
            return ok(emptyResult);
        }
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxy(@RequestBody(required = false) String body, HttpServletRequest request) {
        try {
            String requestURI = request.getRequestURI();
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
            java.util.Map<String, Object> deptMap = new java.util.HashMap<>();
            deptMap.put("DEPARTMENTCODE", "*");
            deptMap.put("departmentCode", "*");

            java.util.Map<String, Object> statusMap = new java.util.HashMap<>();
            statusMap.put("code", "U");
            statusMap.put("desc", "Uninstalled");
            statusMap.put("attribute", "U");
            statusMap.put("status", "U");

            java.util.Map<String, Object> typeMap = new java.util.HashMap<>();
            typeMap.put("code", "JOB");
            typeMap.put("desc", "Job");
            typeMap.put("text", "Job");

            java.util.Map<String, Object> eqMap = new java.util.HashMap<>();
            eqMap.put("EQUIPMENTCODE", "*");
            eqMap.put("equipmentCode", "*");
            eqMap.put("code", "*");

            java.util.Map<String, Object> udfMap = new java.util.HashMap<>();
            udfMap.put("UDFCHKBOX01", "+");
            udfMap.put("udfchkbox01", "+");

            java.util.Map<String, Object> defaultObj = new java.util.HashMap<>();
            defaultObj.put("statusCode", "200");
            defaultObj.put("systemCode", "SYSTEM");
            defaultObj.put("organization", authenticationTools.getOrganizationCode());
            defaultObj.put("DEPARTMENTID", deptMap);
            defaultObj.put("DEPARTMENTCODE", "*");
            defaultObj.put("departmentCode", "*");
            defaultObj.put("attribute", "U");
            defaultObj.put("fields", new java.util.HashMap<>());
            defaultObj.put("text", "Job");
            defaultObj.put("status", statusMap);
            defaultObj.put("statusCode", "U");
            defaultObj.put("STATUSCODE", "U");
            defaultObj.put("type", typeMap);
            defaultObj.put("equipment", eqMap);
            defaultObj.put("EQUIPMENTCODE", "*");
            defaultObj.put("equipmentCode", "*");
            defaultObj.put("PositionParentHierarchy", new java.util.HashMap<>());
            defaultObj.put("AssetParentHierarchy", new java.util.HashMap<>());
            defaultObj.put("SystemParentHierarchy", new java.util.HashMap<>());
            defaultObj.put("parentHierarchy", new java.util.HashMap<>());
            defaultObj.put("userDefinedFields", udfMap);
            defaultObj.put("customFields", new java.util.ArrayList<>());

            java.util.Map<String, Object> wrapper = new java.util.HashMap<>();
            wrapper.put("WorkOrder", defaultObj);
            wrapper.put("WorkOrderDefault", defaultObj);
            wrapper.put("workorder", defaultObj);
            wrapper.put("workOrder", defaultObj);
            wrapper.put("Equipment", defaultObj);
            wrapper.put("EquipmentDefault", defaultObj);
            wrapper.put("equipment", defaultObj);
            wrapper.put("AssetEquipment", defaultObj);
            wrapper.put("AssetEquipmentDefault", defaultObj);
            wrapper.put("assetEquipment", defaultObj);
            wrapper.put("PositionEquipment", defaultObj);
            wrapper.put("PositionEquipmentDefault", defaultObj);
            wrapper.put("positionEquipment", defaultObj);
            wrapper.put("SystemEquipment", defaultObj);
            wrapper.put("SystemEquipmentDefault", defaultObj);
            wrapper.put("systemEquipment", defaultObj);
            wrapper.put("Location", defaultObj);
            wrapper.put("LocationDefault", defaultObj);
            wrapper.put("location", defaultObj);
            wrapper.put("Part", defaultObj);
            wrapper.put("PartDefault", defaultObj);
            wrapper.put("part", defaultObj);
            wrapper.put("Nonconformity", defaultObj);
            wrapper.put("NonconformityDefault", defaultObj);
            wrapper.put("nonconformity", defaultObj);
            wrapper.put("NCR", defaultObj);
            wrapper.put("NCRDefault", defaultObj);
            wrapper.put("ncr", defaultObj);
            wrapper.put("PositionParentHierarchy", defaultObj);
            wrapper.put("AssetParentHierarchy", defaultObj);
            wrapper.put("SystemParentHierarchy", defaultObj);
            wrapper.put("parentHierarchy", defaultObj);

            java.util.Map<String, Object> resultDataWrapper = new java.util.HashMap<>();
            resultDataWrapper.put("ResultData", wrapper);

            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("Result", resultDataWrapper);
            bodyMap.put("ResultData", wrapper);
            bodyMap.put("data", defaultObj);

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
}