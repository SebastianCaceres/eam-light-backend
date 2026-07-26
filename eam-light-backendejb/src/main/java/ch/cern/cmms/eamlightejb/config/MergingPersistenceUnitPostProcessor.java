package ch.cern.cmms.eamlightejb.config;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class MergingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {
    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.entities.InstallParameters");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.grids.entities.GridField");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.grids.entities.GridDataspy");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.grids.entities.GridMetadataRequestResult");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.grids.entities.DataspyField");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.grids.customfields.DataspyCustomField");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.material.entities.Part");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.documents.entities.InforDocEntity");
        pui.addManagedClassName("ch.cern.eam.wshub.core.services.documents.entities.InforDocument");
    }
}
