package com.keehin.kxreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportPathResolverTest {

	@TempDir
	Path reportRoot;

	@Test
	void prefers_app_db_report_if_exists() throws Exception {
		Path expected = reportRoot.resolve("app").resolve("db1").resolve("A00.jasper");
		Files.createDirectories(expected.getParent());
		Files.write(expected, new byte[] { 0 });

		Path actual = ReportPathResolver.resolve(reportRoot, "app", "db1", "A00");
		assertThat(actual).isEqualTo(expected.toAbsolutePath());
	}

	@Test
	void falls_back_to_app_report_if_app_db_missing() throws Exception {
		Path expected = reportRoot.resolve("app").resolve("A00.jasper");
		Files.createDirectories(expected.getParent());
		Files.write(expected, new byte[] { 0 });

		Path actual = ReportPathResolver.resolve(reportRoot, "app", "db1", "A00");
		assertThat(actual).isEqualTo(expected.toAbsolutePath());
	}

	@Test
	void default_report_name_is_A00_when_report_is_null() {
		Path actual = ReportPathResolver.resolve(reportRoot, "app", "db1", null);
		assertThat(actual.getFileName().toString()).isEqualTo("A00.jasper");
	}
}
