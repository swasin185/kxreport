package com.keehin.kxreport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReportApplicationTests {
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void testRoot() {
		String result = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
		assertNotNull(result);
	}
	@Test
	void contextLoads() {
		String pdfFile = this.restTemplate.postForObject("http://localhost:" + port + "/postPDF", null, String.class);
		assertNotNull(pdfFile);
	}
}
