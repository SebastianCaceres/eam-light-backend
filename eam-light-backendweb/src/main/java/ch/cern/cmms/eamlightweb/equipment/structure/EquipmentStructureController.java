package ch.cern.cmms.eamlightweb.equipment.structure;

import ch.cern.cmms.eamlightweb.tools.AuthenticationTools;
import ch.cern.cmms.eamlightweb.tools.EAMLightController;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.tools.InforException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/eqstructure")
public class EquipmentStructureController extends EAMLightController {

    @Autowired
    private AuthenticationTools authenticationTools;
    @Autowired
    private InforClient inforClient;
    @Autowired
    private EquipmentStructure equipmentStructure;

	@GetMapping
	@RequestMapping("/tree")
	public ResponseEntity<?> readEquipmentTree(@RequestParam("eqid") String eqID, @RequestParam("org") String org, @RequestParam("type") String type) {
		try {
			return ok(equipmentStructure.readEquipmentTree(eqID, org, type));
		} catch(Exception e) {
			return ok(new java.util.ArrayList<>());
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
