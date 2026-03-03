package com.keehin.kxreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JasperCompileManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ReportControllerJsonTest {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Path reportDir = createReportDir();

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("kxreport.report.path", () -> reportDir.toAbsolutePath().toString());
		registry.add("kxreport.db.uri", () -> "jdbc:mariadb://localhost:3306/");
		registry.add("kxreport.db.config", () -> "?user=unused&password=unused");
		registry.add("kxreport.db.config.ssl", () -> "?user=unused&password=unused");
	}

	@Test
	void json_returns_report_metadata_for_valid_jasper() throws Exception {
		MvcResult result = mockMvc.perform(get("/json"))
				.andExpect(status().isOk())
				.andReturn();

		Map<String, String>[] body = mapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {
		});

		assertThat(body).isNotNull();
		assertThat(body.length).isGreaterThan(0);
		assertThat(body).anySatisfy(entry -> assertThat(entry.get("file")).isEqualTo("A00"));
	}

	@Test
	void json_is_resilient_to_invalid_jasper_files() throws Exception {
		Files.write(reportDir.resolve("INVALID.jasper"), new byte[] { 1, 2, 3, 4 });

		MvcResult result = mockMvc.perform(get("/json"))
				.andExpect(status().isOk())
				.andReturn();

		Map<String, String>[] body = mapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {
		});

		assertThat(body).anySatisfy(entry -> assertThat(entry.get("file")).contains("INVALID.jasper"));
	}

	private static Path createReportDir() {
		try {
			Path dir = Files.createTempDirectory("kxreport-test-reports-");
			String jrxml = Path.of("report", "A00.jrxml").toAbsolutePath().toString();
			JasperCompileManager.compileReportToFile(jrxml, dir.resolve("A00.jasper").toAbsolutePath().toString());
			return dir;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
