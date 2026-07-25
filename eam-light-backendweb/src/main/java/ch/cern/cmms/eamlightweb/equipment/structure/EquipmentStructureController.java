package ch.cern.cmms.eamlightweb.equipment.structure;

import ch.cern.cmms.eamlightejb.equipment.tools.GraphNode;
import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.client.InforContext;
import ch.cern.eam.wshub.core.services.grids.entities.GridRequest;
import ch.cern.eam.wshub.core.tools.InforException;
import javax.ejb.EJB;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.cmms.eamlightweb.tools.interceptors.RESTLoggingInterceptor;
import ch.cern.cmms.eamlightejb.equipment.EquipmentEJB;

import java.util.LinkedList;
import java.util.List;

@RequestMapping("/eqstructure")

public class EquipmentStructureController extends EAMLightController {



    @Autowired
    private AuthenticationTools authenticationTools;
    @Autowired
    private InforClient inforClient;
    @Autowired EquipmentStructure equipmentStructure;

	@GetMapping
	@RequestMapping("/tree")
	
	
	public ResponseEntity<?> readEquipmentTree(@RequestParam("eqid") String eqID, @RequestParam("org") String org, @RequestParam("type") String type) {
		try {
			return ok(equipmentStructure.readEquipmentTree(eqID, org, type));
		} catch(Exception e) {
			return serverError(e);
		}
	}

	@PostMapping
    @RequestMapping("/attach")
    
    
    public ResponseEntity<?> attachEquipment(ch.cern.eam.wshub.core.services.equipment.entities.EquipmentStructure equipmentStructure){
        try{
            return ok(inforClient.getEquipmentStructureService().addEquipmentToStructure(authenticationTools.getInforContext(), equipmentStructure));
        }catch (InforException ie){
            return serverError(ie);
        }
    }

    @PostMapping
    @RequestMapping("/detach")
    
    
    public ResponseEntity<?> detachEquipment(ch.cern.eam.wshub.core.services.equipment.entities.EquipmentStructure equipmentStructure){
        try{
            return ok(inforClient.getEquipmentStructureService().removeEquipmentFromStructure(authenticationTools.getInforContext(), equipmentStructure));
        }catch (InforException ie){
            return serverError(ie);
        }
    }

}
