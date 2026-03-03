package com.keehin.kxreport;

import java.nio.file.Files;
import java.nio.file.Path;

final class ReportPathResolver {
	static final String JASPER_EXT = ".jasper";

	private ReportPathResolver() {
	}

	static Path resolve(Path reportRoot, String app, String db, String report) {
		String jasperFileName = ((report != null) ? report : "A00") + JASPER_EXT;

		Path root = reportRoot;
		if (app != null && !app.isBlank())
			root = root.resolve(app);

		String dbPath = (db != null) ? db : "";
		Path firstLookDbPath = root.resolve(dbPath).resolve(jasperFileName);
		if (Files.exists(firstLookDbPath))
			return firstLookDbPath.toAbsolutePath();

		return root.resolve(jasperFileName).toAbsolutePath();
	}
}
