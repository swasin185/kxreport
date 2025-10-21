package com.keehin.kxreport;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin // (origins = "http://localhost:8080", allowCredentials = "true")
@RestController
public class ReportController {
	private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
	public final SimpleDateFormat DTFormat = new SimpleDateFormat("dd/MM/yy [HH:mm:ss]", Locale.US);
	private static String JASPER = ".jasper";
	private Database db;
	private SimpleCsvExporterConfiguration config;
	private JRCsvExporter exporter;

	public ReportController() {
		try {
			logger.info("KXREPORT WELCOME !!!");
			db = new Database();
			Files.createDirectories(Paths.get(Database.getOUTPUT_PATH()));
			config = new SimpleCsvExporterConfiguration();
			config.setFieldDelimiter("\t");
			exporter = new JRCsvExporter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(JASPER);
		}
	};

	private void createTempIdList(Connection conn, String idList) throws SQLException {
		if (conn != null && idList != null && !idList.isEmpty()) {
			Statement stmt = conn.createStatement();
			stmt.execute("drop report temporary table if exists idlist");
			stmt.execute("create temporary table idlist(id varchar(50))");
			stmt.execute("insert into idlist values " + idList);
		}
	}

	private JasperPrint loadJasperFile(Map<String, Object> params) {
		JasperPrint report = null;
		Connection conn = db.getConnection((String) params.get("db"));
		try {
			this.createTempIdList(conn, (String) params.get("idList"));
			report = JasperFillManager
					.fillReport((JasperReport) JRLoader.loadObjectFromFile(getJasperFile(params)), params, conn);
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

	private static void logging(HttpServletRequest request) {
		logger.info(String.format("%-16s", request.getRemoteAddr()) + request.getRequestURI());
	}

	private String getJasperFile(Map<String, Object> params) {
		String appName = "";
		String dbName = "";
		String jasperFile = (String) params.get("report");
		if (jasperFile != null) {
			if (!jasperFile.contains(JASPER))
				jasperFile += JASPER;
		} else {
			jasperFile = "A00" + JASPER;
		}
		if (params.get("app") != null)
			appName = params.get("app").toString() + "/";
		if (params.get("db") != null)
			dbName = params.get("db").toString() + "/";
		File jpFile = new File(Database.getREPORT_PATH() + appName + dbName + jasperFile);
		if (!jpFile.exists())
			jpFile = new File(Database.getREPORT_PATH() + appName + jasperFile);
		return jpFile.getAbsolutePath();
	}

	@SuppressWarnings("unchecked")
	@GetMapping(value = "/json", produces = "application/json; charset=UTF-8")
	public ResponseEntity<Map<String, String>[]> json() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		File dir = new File(Database.getREPORT_PATH());
		if (dir.exists()) {
			String[] list = dir.list(ReportController.filter);
			for (String fileName : list)
				try {
					File file = new File(Database.getREPORT_PATH() + fileName);
					JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(file.getPath());
					if (report != null) {
						Map<String, String> reportData = new HashMap<>();
						reportData.put("report", file.getName().substring(0, file.getName().indexOf(JASPER)));
						reportData.put("name", report.getName());
						reportData.put("updated", this.DTFormat.format(file.lastModified()));
						long kilobytes = file.length() / 1024;
						reportData.put("size", String.valueOf(kilobytes));
						data.add(reportData);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		Map<String, String>[] json = data.toArray(new Map[0]);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
	}

	@GetMapping(value = "/list", produces = "text/html; charset=UTF-8")
	public String list(HttpServletRequest request) {
		ReportController.logging(request);
		ResponseEntity<Map<String, String>[]> response = this.json();
		Map<String, String>[] data = response.getBody();

		String html = "<html><head><style>table, th, td { border: 1px solid black; text-align: center; } table { border-collapse: collapse; width: 100%; }</style></head><body>";
		StringBuilder sb = new StringBuilder(html);
		sb.append("report files in " + Database.getREPORT_PATH() + "<br>");
		sb.append("session ID: " + request.getSession().getId() + " - cookie: " + request.getCookies()
				+ "<br><br><table>");
		sb.append("<tr><th>#</th><th>File Name</th><th>Report Name</th><th>Last Updated</th><th>Size(K)</th></tr>");
		if (data != null && data.length > 0) {
			int i = 0;
			for (Map<String, String> reportData : data) {
				i++;
				sb.append("<tr>");
				sb.append("<td>" + i + "</td>");
				sb.append("<td>" + reportData.get("report") + "</td>");
				sb.append("<td>" + reportData.get("name") + "</td>");
				sb.append("<td>" + reportData.get("updated") + "</td>");
				sb.append("<td>" + reportData.get("size") + "</td>");
				sb.append("</tr>");
			}
		} else
			sb.append("<tr><td colspan='4'>No reports found or an error occurred.</td></tr>");

		sb.append("</table></body></html>");
		return sb.toString();
	}

	@GetMapping(value = "/getPDF", produces = "text/html; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> getPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		return this.openPDF(request, session, params);
	}

	private void removeEmpty(Map<String, Object> params) {
		params.entrySet().removeIf(
				e -> e.getValue() == null || e.getValue().toString().isEmpty());
	}

	@PostMapping(value = "/openPDF", produces = "application/pdf")
	public ResponseEntity<StreamingResponseBody> openPDF(HttpServletRequest request, HttpSession session,
			@RequestBody Map<String, Object> params) {
		ReportController.logging(request);
		this.removeEmpty(params);
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
	public String filePDF(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> params) {
		ReportController.logging(request);
		this.removeEmpty(params);
		String outputFile = null;
		String sessId = SessionListener.createHashCode(session);
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			outputFile = sessId + "/" + params.get(params.get("saveFile") == null ? "report" : "saveFile").toString()
					+ ".pdf";
			JasperExportManager.exportReportToPdfStream(jasperPrint,
					new FileOutputStream(Database.getOUTPUT_PATH() + outputFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}

	@PostMapping(value = "/openCSV", produces = "text/csv; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> openCSV(HttpServletRequest request, HttpSession session,
			@RequestBody Map<String, Object> params) {
		ReportController.logging(request);
		this.removeEmpty(params);
		StreamingResponseBody responseBody = null;
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			responseBody = outputStream -> {
				try {
					exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
					exporter.setConfiguration(this.config);
					exporter.exportReport();
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
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + params.get(params.get("saveFile") == null ? "report" : "saveFile")
									+ ".csv")
					.body(responseBody);
	}

	@PostMapping(value = "/fileCSV", produces = "text/plain")
	public String fileCSV(HttpServletRequest request, HttpSession session, @RequestBody Map<String, Object> params) {
		ReportController.logging(request);
		this.removeEmpty(params);
		String outputFile = null;
		String sessId = SessionListener.createHashCode(session);
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			outputFile = sessId + "/" + params.get(params.get("saveFile") == null ? "report" : "saveFile").toString()
					+ ".csv";
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(
					new SimpleWriterExporterOutput(new FileOutputStream(Database.getOUTPUT_PATH() + outputFile)));
			exporter.setConfiguration(this.config);
			exporter.exportReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}
}