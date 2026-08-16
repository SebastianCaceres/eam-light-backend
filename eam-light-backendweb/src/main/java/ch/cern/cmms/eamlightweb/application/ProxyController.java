package ch.cern.cmms.eamlightweb.application;

import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightNativeRestController;
import ch.cern.cmms.eamlightweb.tools.EntityDefaultsService;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.interceptors.InforInterceptor;
import ch.cern.eam.wshub.core.interceptors.beans.InforErrorData;
import ch.cern.eam.wshub.core.interceptors.beans.InforExtractedData;
import ch.cern.eam.wshub.core.interceptors.beans.InforRequestData;
import ch.cern.eam.wshub.core.interceptors.beans.InforResponseData;
import ch.cern.eam.wshub.core.services.INFOR_OPERATION;
import ch.cern.eam.wshub.core.services.equipment.entities.Category;
import ch.cern.eam.wshub.core.tools.InforException;

import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private EntityDefaultsService entityDefaultsService;

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
            Category category = inforClient.getCategoryService().readCategory(authenticationTools.getInforContext(), categoryCode);
            return ok(category);
        } catch(Exception e) {
            return ok(new java.util.HashMap<>());
        }
    }

    @GetMapping("/customfields")
    public ResponseEntity<?> readCustomFields(@RequestParam(value = "entityCode", required = false, defaultValue = "") String entityCode, @RequestParam(value = "classCode", required = false, defaultValue = "*") String classCode) {
        java.util.Map<String, Object> cfMap = new java.util.HashMap<>();
        cfMap.put("CUSTOMFIELD", new java.util.ArrayList<>());
        cfMap.put("customFields", new java.util.ArrayList<>());
        cfMap.put("ResultData", new java.util.ArrayList<>());
        return ok(cfMap);
    }

    @GetMapping("/positionparenthierarchy")
    public ResponseEntity<?> readPositionHierarchy(@RequestParam("code") String code, @RequestParam("org") String org) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("POSITIONPARENTHIERARCHY", new java.util.ArrayList<>());
        return ok(result);
    }

    @PostMapping("/grids")
    public ResponseEntity<?> proxyGrids(@RequestBody GridRequest gridRequest) {
        try {
            return ok(inforClient.getGridsService().executeQuery(authenticationTools.getInforContext(), gridRequest));
        } catch (Exception e) {
            return ok(entityDefaultsService.createDefaultGridResultMap());
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
                return ok(entityDefaultsService.createDefaultListResponse());
            }

            return ok(entityDefaultsService.createDefaultEntityResponse());
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