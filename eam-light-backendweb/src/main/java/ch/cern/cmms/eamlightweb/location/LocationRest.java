package ch.cern.cmms.eamlightweb.location;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightNativeRestController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.services.entities.UserDefinedFields;
import ch.cern.eam.wshub.core.services.equipment.entities.Location;
import ch.cern.eam.wshub.core.tools.InforException;
import static ch.cern.eam.wshub.core.tools.Tools.extractEntityCode;
import static ch.cern.eam.wshub.core.tools.Tools.extractOrganizationCode;
import net.datastream.schemas.mp_fields.LOCATIONID_Type;
import net.datastream.schemas.mp_fields.ORGANIZATIONID_Type;
import net.datastream.schemas.mp_functions.mp0318_001.MP0318_GetLocation_001;
import net.datastream.schemas.mp_functions.mp0319_001.MP0319_SyncLocation_001;
import net.datastream.schemas.mp_results.mp0318_001.MP0318_GetLocation_001_Result;
import net.datastream.schemas.mp_results.mp0319_001.MP0319_SyncLocation_001_Result;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.xml.ws.soap.SOAPFaultException;

@RestController
@RequestMapping("/locations")

public class LocationRest extends EAMLightNativeRestController {

    @Autowired
    private AuthenticationTools authenticationTools;

    @Autowired
    private InforClient inforClient;

    @GetMapping("/{location}")
    public ResponseEntity<?> readLocation(@PathVariable("location") String location) {
        if (location != null && (location.equalsIgnoreCase("new") || location.toLowerCase().startsWith("new"))) {
            return initLocation();
        }
        try {
            MP0318_GetLocation_001 getLocation = new MP0318_GetLocation_001();
            getLocation.setLOCATIONID(new LOCATIONID_Type());
            getLocation.getLOCATIONID().setORGANIZATIONID(new ORGANIZATIONID_Type());
            getLocation.getLOCATIONID().getORGANIZATIONID().setORGANIZATIONCODE(extractOrganizationCode(location));
            getLocation.getLOCATIONID().setLOCATIONCODE( extractEntityCode(location) );

            MP0318_GetLocation_001_Result getLocationResult = inforClient.getTools().performInforOperation(authenticationTools.getInforContext(), inforClient.getInforWebServicesToolkitClient()::getLocationOp , getLocation);

            return ok(getLocationResult);
        } catch (SOAPFaultException e) {
            return badRequest(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createLocation(Location location) {
        try {
            return ok(inforClient.getLocationService().createLocation(authenticationTools.getInforContext(), location));
        } catch (SOAPFaultException e) {
            return badRequest(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateLocation(net.datastream.schemas.mp_entities.location_001.Location location) {
        try {
            MP0319_SyncLocation_001 syncLocation = new MP0319_SyncLocation_001();
            syncLocation.setLocation(location);
            MP0319_SyncLocation_001_Result result =  inforClient.getTools().performInforOperation(authenticationTools.getInforContext(), inforClient.getInforWebServicesToolkitClient()::syncLocationOp , syncLocation);
            return ok(result);
        } catch (InforException e) {
            return badRequest(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @DeleteMapping("/{locationCode:.+}")
    public ResponseEntity<?> deleteLocation(@PathVariable("locationCode") String locationCode) {
        try {
            return ok(inforClient.getLocationService().deleteLocation(authenticationTools.getInforContext(), locationCode));
        } catch (InforException e) {
            return badRequest(e);
        } catch (Exception e) {
            return serverError(e);
        }
    }

    @GetMapping({"/init", "/new"})
    public ResponseEntity<?> initLocation() {
        Location location = new Location();
        location.setUserDefinedFields(new UserDefinedFields());
        try {
            location.setCustomFields(inforClient.getTools().getCustomFieldsTools()
                .getWSHubCustomFields(authenticationTools.getInforContext(), "LOC", "*"));
        } catch (Exception e) {
            location.setCustomFields(new ch.cern.eam.wshub.core.services.entities.CustomField[0]);
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("Location", location);
        map.put("LocationDefault", location);
        map.put("location", location);
        return ok(map);
    }
}
