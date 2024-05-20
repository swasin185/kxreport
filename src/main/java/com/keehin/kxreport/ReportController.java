package com.keehin.kxreport;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

// @CrossOrigin (origins = "http://localhost:8080", allowCredentials = "true")
@CrossOrigin
@RestController
public class ReportController {

	private Database db;
	private SimpleCsvExporterConfiguration config;
	
	public ReportController() {
		try {
			db = new Database();
			Files.createDirectories(Paths.get(Database.OUTPUT_PATH));
			config = new SimpleCsvExporterConfiguration();
			config.setFieldDelimiter("\t");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JasperPrint loadJasperFile(Map<String, Object> params) throws Exception {
		Connection conn = db.getConnection((String) params.get("db"));
		this.createTempIdList(conn, (String) params.get("idList"));
		JasperPrint p = JasperFillManager
				.fillReport((JasperReport) JRLoader.loadObjectFromFile(getJasperFile(params)), params, conn);
		conn.close();
		return p;
	}

	private String getJasperFile(Map<String, Object> params) {
		String appName = "";
		String dbName = "";
		String jasperFile = (String) params.get("report");
		if (jasperFile != null) {
			if (!jasperFile.contains(".jasper"))
				jasperFile += ".jasper";
		} else {
			jasperFile = "00-000.jasper";
		}
		if (params.get("app") != null)
			appName = "-" + params.get("app").toString();
		dbName = (String) params.get("db");
		File jpFile = new File(Database.REPORT_PATH + appName + "/" + dbName + "/" + jasperFile);
		if (!jpFile.exists())
			jpFile = new File(Database.REPORT_PATH + "/" + jasperFile);
		return jpFile.getAbsolutePath();
	}

	private void logClient(HttpServletRequest req, Map<String, Object> params) {
		System.out.println(req.getRemoteAddr() + "," + (String) params.get("db") + "," + (String) params.get("report"));
	}

	private Map<String, Object> mapParams(Parameter params) {
		Map<String, Object> mapParams = new HashMap<String, Object>();
		if (params.getApp() != null)
			mapParams.put("app", params.getApp());
		if (params.getDb() != null)
			mapParams.put("db", params.getDb());
		if (params.getComCode() != null)
			mapParams.put("comCode", params.getComCode());
		if (params.getComName() != null)
			mapParams.put("comName", params.getComName());
		if (params.getReport() != null)
			mapParams.put("report", params.getReport());
		if (params.getFromDate() != null)
			mapParams.put("fromDate", params.getFromDate());
		if (params.getToDate() != null)
			mapParams.put("toDate", params.getToDate());
		if (params.getFromId() != null)
			mapParams.put("fromId", params.getFromId());
		if (params.getToId() != null)
			mapParams.put("toId", params.getToId());
		if (params.getOption() != null)
			mapParams.put("option", params.getOption());
		return mapParams;
	}

	// @GetMapping
	// public String index() {
	// return "KxReport KEEHIN Jasper Report API";
	// }

	@GetMapping(path = "/list")
	public String list(HttpServletRequest req, HttpServletResponse response) {
		String html = "<html><head><style>table, th, td { border: 1px solid black;} table {width: 100%;}</style></head><body>";
		StringBuilder sb = new StringBuilder(html);
		sb.append("report files in " + Database.REPORT_PATH + "<br>");
		sb.append("session ID: " + req.getSession().getId() + " - cookie: " + req.getCookies() + "<br><br><table>");
		File dir = new File(Database.REPORT_PATH);
		if (dir.exists()) {
			int i = 0;
			String[] list = dir.list();
			Arrays.sort(list);
			for (String fileName : list) {
				File file = new File(Database.REPORT_PATH + fileName);
				sb.append("<tr>");
				sb.append("<td>" + (++i) + "</td>");
				sb.append("<td>" + fileName + "</td>");
				sb.append("<td>" + Database.DTFormat.format(file.lastModified()) + "</td>");
				sb.append("</tr>");
			}
		}
		sb.append("</table></body></html>");
		return sb.toString();
	}

	@GetMapping("/getPDF")
	public ResponseEntity<StreamingResponseBody> getPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		return openPDF(request, session, params);
	}

	@PostMapping("/execPDF")
	public ResponseEntity<StreamingResponseBody> execPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Parameter params) {
		return openPDF(request, session, this.mapParams(params));
	}

	// @PostMapping(path = "/openPDF", produces = "application/pdf")
	@PostMapping("/openPDF")
	public ResponseEntity<StreamingResponseBody> openPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		this.logClient(request, params);
		StreamingResponseBody responseBody = null;
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			responseBody = outputStream -> {
				try {
					JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			};
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		if (responseBody == null)
			return ResponseEntity.notFound().build();
		else
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline")
					.contentType(MediaType.APPLICATION_PDF).body(responseBody);
	}

	private void createTempIdList(Connection conn, String idList) throws SQLException {
		if (idList != null) {
			Statement stmt = conn.createStatement();
			stmt.execute("drop temporary table if exists idlist");
			stmt.execute("create temporary table idlist(id varchar(50))");
			stmt.execute("insert into idlist values " + idList);
		}
	}

	@PostMapping("/openCSV")
	public ResponseEntity<StreamingResponseBody> openCSV(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		this.logClient(request, params);
		StreamingResponseBody responseBody = null;
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			responseBody = outputStream -> {
				try {
					JRCsvExporter exporter = new JRCsvExporter();
					exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
					exporter.setConfiguration(this.config);
					exporter.exportReport();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			};
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		if (responseBody == null)
			return ResponseEntity.notFound().build();
		else
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
					.contentType(MediaType.TEXT_PLAIN).body(responseBody);
	}

	// @PostMapping(path = "/filePDF", produces = "text/plain")
	@PostMapping("/filePDF") // need RestController
	public String filePDF(HttpServletRequest request, HttpSession session, @RequestParam Map<String, Object> params) {
		this.logClient(request, params);
		String jasperFile = getJasperFile(params);
		String sessId = Integer.toString(session.getId().hashCode(), 16).toUpperCase();
		String outputFile = jasperFile;
		File dir = new File(Database.OUTPUT_PATH + sessId);
		if (!dir.exists())
			dir.mkdirs();
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			outputFile = sessId + Integer.toString(jasperPrint.hashCode(), 16) + ".pdf";
			JasperExportManager.exportReportToPdfStream(jasperPrint,
					new FileOutputStream(Database.OUTPUT_PATH + outputFile));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return outputFile;
	}
}