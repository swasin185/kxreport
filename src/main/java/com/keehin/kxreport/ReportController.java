package com.keehin.kxreport;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.stream.Stream;

import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin
@RestController
public class ReportController {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private Database db;

	private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
	private static final SimpleDateFormat dtFormat = new SimpleDateFormat("dd/MM/yy [HH:mm:ss]", Locale.US);
	private static final String JASPER = ".jasper";
	private static final SimpleCsvExporterConfiguration config = new SimpleCsvExporterConfiguration();

	@SuppressWarnings("unchecked")
	@GetMapping(value = "/json", produces = "application/json; charset=UTF-8")
	public ResponseEntity<Map<String, String>[]> json() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		Path reportDir = Paths.get(Database.getReportPath());
		if (Files.exists(reportDir) && Files.isDirectory(reportDir)) {
			List<Path> paths = new ArrayList<>();
			try (Stream<Path> walk = Files.walk(reportDir)) {
				walk.forEach(paths::add); // collect all paths first
			} catch (IOException e) {
				logger.error("Error walking directory: {}", Database.getReportPath(), e);
			}
			paths.sort(Comparator.naturalOrder());
			Map<String, String> reportData = new HashMap<>();
			for (Path path : paths) {
				reportData = new HashMap<>();
				try {
					JasperReport report = (JasperReport) JRLoader
							.loadObjectFromFile(path.toAbsolutePath().toString());

					if (report != null) {
						String fileName = path.getFileName().toString();
						String nameWithoutExt = fileName.contains(JASPER)
								? fileName.substring(0, fileName.indexOf(JASPER))
								: fileName;
						reportData.put("file", nameWithoutExt);
						reportData.put("report", report.getName());
						reportData.put("updated", dtFormat.format(Files.getLastModifiedTime(path).toMillis()));
						long kilobytes = Files.size(path) >> 10;
						reportData.put("size", String.valueOf(kilobytes));
						data.add(reportData);
					}
				} catch (Exception e) {
					reportData.put("file", path.toAbsolutePath().toString());
					data.add(reportData);
				}
			}
		}
		Map<String, String>[] json = data.toArray(new Map[0]);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
	}

	@GetMapping(value = "/getPDF", produces = "text/html; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> getPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		return this.openPDF(request, session, mapper.convertValue(params, Parameter.class));
	}

	@PostMapping(value = "/openPDF", produces = "application/pdf")
	public ResponseEntity<StreamingResponseBody> openPDF(HttpServletRequest request, HttpSession session,
			@RequestBody Parameter params) {
		logger.info("{}\t{}", request.getRequestURI(), request.getRemoteAddr());
		StreamingResponseBody responseBody = null;
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			responseBody = outputStream -> {
				try {
					JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			};
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		if (responseBody == null)
			return ResponseEntity.notFound().build();
		else
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline;")
					.contentType(MediaType.APPLICATION_PDF).body(responseBody);
	}

	@PostMapping(value = "/filePDF", produces = "text/plain")
	public String filePDF(HttpServletRequest request, HttpSession session, @RequestBody Parameter params) {
		logger.info("{}\t{}", request.getRequestURI(), request.getRemoteAddr());
		String outputFile = getOutputFile(params, SessionListener.createHashCode(session)) + ".pdf";
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			JasperExportManager.exportReportToPdfStream(jasperPrint,
					new FileOutputStream(Database.ROOT + request.getContextPath() + outputFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}

	@PostMapping(value = "/fileCSV", produces = "text/plain")
	public String fileCSV(HttpServletRequest request, HttpSession session, @RequestBody Parameter params) {
		logger.info("{}\t{}", request.getRequestURI(), request.getRemoteAddr());
		String outputFile = getOutputFile(params, SessionListener.createHashCode(session)) + ".csv";
		JRCsvExporter exporter = new JRCsvExporter();
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(
					new SimpleWriterExporterOutput(
							new FileOutputStream(Database.ROOT + request.getContextPath() + outputFile)));
			exporter.setConfiguration(config);
			exporter.exportReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}

	private void createTempIdList(Connection conn, String idList) throws SQLException {
		if (conn != null && idList != null && !idList.isEmpty()) {
			Statement stmt = conn.createStatement();
			stmt.execute("drop report temporary table if exists idlist");
			stmt.execute("create temporary table idlist(id varchar(50))");
			PreparedStatement ps = conn.prepareStatement("INSERT INTO idlist VALUES (?)");
			for (String item : idList.split(",")) {
				ps.setString(1, item.trim());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private JasperPrint loadJasperFile(Parameter params) {
		JasperPrint report = null;
		Connection conn = db.getConnection((String) params.getDb());
		try {
			this.createTempIdList(conn, (String) params.getIdList());
			report = JasperFillManager
					.fillReport((JasperReport) JRLoader.loadObjectFromFile(getJasperFile(params)),
							mapper.convertValue(params, new TypeReference<>() {
							}),
							conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return report;
	}

	private String getJasperFile(Parameter params) {
		String jasperFileName = ((params.getReport() != null) ? params.getReport() : "A00") + JASPER;

		String appPath = (params.getApp() != null) ? params.getApp() : "";
		String dbPath = (params.getDb() != null) ? params.getDb() : "";

		Path reportRoot = Paths.get(Database.getReportPath());
		if (!appPath.equals(""))
			reportRoot = reportRoot.resolve(appPath);

		Path firstLookDbPath = reportRoot.resolve(dbPath).resolve(jasperFileName);
		if (Files.exists(firstLookDbPath))
			return firstLookDbPath.toAbsolutePath().toString();

		return reportRoot.resolve(jasperFileName).toAbsolutePath().toString();
	}

	private static String getOutputFile(Parameter params, String sessId) {
		return "/" + sessId + "/" + (params.getSaveFile() == null ? params.getReport()
				: params.getSaveFile());
	}

}