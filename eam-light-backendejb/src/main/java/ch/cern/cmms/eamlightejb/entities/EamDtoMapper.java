package ch.cern.cmms.eamlightejb.entities;

import org.springframework.beans.BeanUtils;

public class EamDtoMapper {

    public static Equipment map(ch.cern.eam.wshub.core.services.equipment.entities.Equipment soapEq) {
        if (soapEq == null) return null;
        Equipment localEq = new Equipment();
        BeanUtils.copyProperties(soapEq, localEq);
        if (soapEq.getUserDefinedFields() != null) {
            UserDefinedFields udf = new UserDefinedFields();
            BeanUtils.copyProperties(soapEq.getUserDefinedFields(), udf);
            localEq.setUserDefinedFields(udf);
        }
        if (soapEq.getCustomFields() != null) {
            ch.cern.eam.wshub.core.services.entities.CustomField[] soapCf = soapEq.getCustomFields();
            CustomField[] localCf = new CustomField[soapCf.length];
            for (int i = 0; i < soapCf.length; i++) {
                if (soapCf[i] != null) {
                    localCf[i] = new CustomField();
                    BeanUtils.copyProperties(soapCf[i], localCf[i]);
                }
            }
            localEq.setCustomFields(localCf);
        }
        return localEq;
    }

    public static ch.cern.eam.wshub.core.services.equipment.entities.Equipment map(Equipment localEq) {
        if (localEq == null) return null;
        ch.cern.eam.wshub.core.services.equipment.entities.Equipment soapEq = new ch.cern.eam.wshub.core.services.equipment.entities.Equipment();
        BeanUtils.copyProperties(localEq, soapEq);
        if (localEq.getUserDefinedFields() != null) {
            ch.cern.eam.wshub.core.services.entities.UserDefinedFields udf = new ch.cern.eam.wshub.core.services.entities.UserDefinedFields();
            BeanUtils.copyProperties(localEq.getUserDefinedFields(), udf);
            soapEq.setUserDefinedFields(udf);
        }
        if (localEq.getCustomFields() != null) {
            CustomField[] localCf = localEq.getCustomFields();
            ch.cern.eam.wshub.core.services.entities.CustomField[] soapCf = new ch.cern.eam.wshub.core.services.entities.CustomField[localCf.length];
            for (int i = 0; i < localCf.length; i++) {
                if (localCf[i] != null) {
                    soapCf[i] = new ch.cern.eam.wshub.core.services.entities.CustomField();
                    BeanUtils.copyProperties(localCf[i], soapCf[i]);
                }
            }
            soapEq.setCustomFields(soapCf);
        }
        return soapEq;
    }

    public static WorkOrder map(ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder soapWo) {
        if (soapWo == null) return null;
        WorkOrder localWo = new WorkOrder();
        BeanUtils.copyProperties(soapWo, localWo);
        return localWo;
    }

    public static ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder map(WorkOrder localWo) {
        if (localWo == null) return null;
        ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder soapWo = new ch.cern.eam.wshub.core.services.workorders.entities.WorkOrder();
        BeanUtils.copyProperties(localWo, soapWo);
        return soapWo;
    }
}
