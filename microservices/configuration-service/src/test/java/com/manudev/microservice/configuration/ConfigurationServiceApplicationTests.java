package com.manudev.microservice.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"configuration.admin.username=test-admin",
		"configuration.admin.password=test-password"
})
@AutoConfigureMockMvc
class ConfigurationServiceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void configurationWritesRequireAdministratorAuthentication() throws Exception {
		mockMvc.perform(patch("/api/v1/configuracion/notificaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"weeklyReports\":true}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void configurationHistoryRequiresAdministratorAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/configuracion/historial"))
				.andExpect(status().isUnauthorized());
	}
}
