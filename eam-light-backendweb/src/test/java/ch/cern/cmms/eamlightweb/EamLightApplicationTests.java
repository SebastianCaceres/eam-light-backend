package ch.cern.cmms.eamlightweb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import ch.cern.eam.wshub.core.client.InforClient;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class EamLightApplicationTests {

    static {
        System.setProperty("EAMLIGHT_INFOR_WS_URL", "http://localhost:8080/dummy");
        System.setProperty("EAMLIGHT_INFOR_TENANT", "dummy");
        System.setProperty("EAMLIGHT_INFOR_ORGANIZATION", "*");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InforClient inforClient;

    @Test
    public void contextLoads() {
        // Verifies that the Spring Boot application context starts cleanly
    }

    @Test
    public void printUserSetupServiceInfo() throws Exception {
        Class<?> clazz = inforClient.getUserSetupService().getClass();
        printClassInfo(clazz.getName());
    }

    private void printClassInfo(String className) throws Exception {
        System.out.println("=================================================");
        System.out.println("CLASS: " + className);
        Class<?> clazz = Class.forName(className);
        System.out.println("METHODS:");
        for (java.lang.reflect.Method m : clazz.getDeclaredMethods()) {
            System.out.print("  " + m.getReturnType().getSimpleName() + " " + m.getName() + "(");
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                System.out.print(params[i].getSimpleName() + (i < params.length - 1 ? ", " : ""));
            }
            System.out.println(")");
        }
        System.out.println("FIELDS:");
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            System.out.println("  " + f.getType().getSimpleName() + " " + f.getName());
        }
    }

    @Test
    public void checkApplicationVersionUnauthorized() throws Exception {
        // Unauthenticated requests should receive a 401 Unauthorized
        mockMvc.perform(get("/application/version"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void checkApplicationVersionAuthorized() throws Exception {
        // Authenticated requests should succeed
        mockMvc.perform(get("/application/version"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testWorkOrderCRUD() throws Exception {
        // 1. Create a Work Order
        String woJson = "{\"description\":\"Test WO Description\",\"equipmentCode\":\"EQ-12345\"}";
        String response = mockMvc.perform(post("/proxy/workorders")
                .with(csrf())
                .contentType("application/json")
                .content(woJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").exists())
                .andExpect(jsonPath("$.data.description").value("Test WO Description"))
                .andReturn().getResponse().getContentAsString();

        // Extract generated code
        String woCode = com.jayway.jsonpath.JsonPath.read(response, "$.data.code");

        // 2. Read the Work Order
        mockMvc.perform(get("/proxy/workorders/" + woCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value(woCode))
                .andExpect(jsonPath("$.data.description").value("Test WO Description"));

        // 3. Update the Work Order
        String updatedWoJson = "{\"description\":\"Updated WO Description\",\"equipmentCode\":\"EQ-12345\"}";
        mockMvc.perform(put("/proxy/workorders/" + woCode)
                .with(csrf())
                .contentType("application/json")
                .content(updatedWoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated WO Description"));

        // 4. Delete the Work Order
        mockMvc.perform(delete("/proxy/workorders/" + woCode)
                .with(csrf()))
                .andExpect(status().isOk());

        // 5. Verify it's deleted
        mockMvc.perform(get("/proxy/workorders/" + woCode))
                .andExpect(status().isNotFound());
    }
}
