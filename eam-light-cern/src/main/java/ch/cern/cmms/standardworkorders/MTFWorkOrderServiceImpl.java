package ch.cern.cmms.standardworkorders;

import ch.cern.eam.wshub.core.client.InforClient;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class MTFWorkOrderServiceImpl implements MTFWorkOrderService {

    @Autowired
    InforClient inforClient;

    @Override
    public MTFWorkOrder getEquipmentStandardWOMaxStep(String eqCode, String swo) {
        MTFWorkOrderImpl results = inforClient.getTools().getEntityManager()
                .createNamedQuery(MTFWorkOrderImpl.GET_EQUIPMENT_SWO_MAX_STEP, MTFWorkOrderImpl.class)
                .setParameter("eqCode", eqCode)
                .setParameter("swo", swo).getSingleResult();

        return results;
    }
}
