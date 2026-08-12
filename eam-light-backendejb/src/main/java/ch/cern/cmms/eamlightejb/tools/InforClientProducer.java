package ch.cern.cmms.eamlightejb.tools;

import ch.cern.cmms.eamlightejb.cache.ExternalCache;
import ch.cern.cmms.eamlightejb.data.ApplicationData;
import ch.cern.eam.wshub.core.client.InforClient;
import ch.cern.eam.wshub.core.interceptors.InforInterceptor;
import ch.cern.eam.wshub.core.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.logging.Logger;

@Configuration
public class InforClientProducer {

    @Autowired
    private ApplicationData applicationData;

    @Autowired
    private ExternalCache externalCache;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private InforInterceptor inforInterceptor;

    @Autowired(required = false)
    private WorkOrderRepository workOrderRepository;

    @Autowired(required = false)
    private ActivityRepository activityRepository;

    @Autowired(required = false)
    private LaborBookingRepository laborBookingRepository;

    @Autowired(required = false)
    private FindingRepository findingRepository;

    @Autowired(required = false)
    private PartRepository partRepository;

    @Autowired(required = false)
    private EquipmentRepository equipmentRepository;

    @Autowired(required = false)
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private CategoryRepository categoryRepository;

    @Autowired(required = false)
    private InforDocumentRepository inforDocumentRepository;

    @Autowired(required = false)
    private InforDocEntityRepository inforDocEntityRepository;

    @Autowired(required = false)
    private InforCaseRepository inforCaseRepository;

    @Autowired(required = false)
    private InforCaseTaskRepository inforCaseTaskRepository;

    @Autowired(required = false)
    private EquipmentPMScheduleRepository equipmentPMScheduleRepository;

    @Autowired(required = false)
    private EquipmentWarrantyRepository equipmentWarrantyRepository;

    @Autowired(required = false)
    private EAMUserRepository eamUserRepository;

    @Bean
    public InforClient inforClient() {
        try {
            String inforWsUrl = Tools.getVariableValue("EAMLIGHT_INFOR_WS_URL");
            if (inforWsUrl == null || inforWsUrl.trim().isEmpty()) {
                inforWsUrl = "http://localhost/inforws";
            }

            InforClient inforClient = new InforClient.Builder(inforWsUrl)
                    .withDefaultTenant(Tools.getVariableValue("EAMLIGHT_INFOR_TENANT"))
                    .withDefaultOrganizationCode(Tools.getVariableValue("EAMLIGHT_INFOR_ORGANIZATION"))
                    .withDataSource(dataSource)
                    .withEntityManagerFactory(entityManagerFactory)
                    .withInforInterceptor(inforInterceptor)
                    .withLogger(Logger.getLogger("wshublogger"))
                    .withCache(externalCache.getCacheMap())
                    .withWorkOrderRepository(workOrderRepository)
                    .withActivityRepository(activityRepository)
                    .withLaborBookingRepository(laborBookingRepository)
                    .withFindingRepository(findingRepository)
                    .withPartRepository(partRepository)
                    .withEquipmentRepository(equipmentRepository)
                    .withEmployeeRepository(employeeRepository)
                    .withCategoryRepository(categoryRepository)
                    .withInforDocumentRepository(inforDocumentRepository)
                    .withInforDocEntityRepository(inforDocEntityRepository)
                    .withInforCaseRepository(inforCaseRepository)
                    .withInforCaseTaskRepository(inforCaseTaskRepository)
                    .withEquipmentPMScheduleRepository(equipmentPMScheduleRepository)
                    .withEquipmentWarrantyRepository(equipmentWarrantyRepository)
                    .withEAMUserRepository(eamUserRepository)
                    .localizeResults(false)
                    .build();

            return inforClient;
        } catch (Exception exception) {
            System.out.println("Infor Client could not be initialized: " + exception.getMessage());
            exception.printStackTrace();
            throw new IllegalStateException("Failed to initialize InforClient", exception);
        }
    }

}