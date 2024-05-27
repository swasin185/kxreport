package com.keehin.kxreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
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

@CrossOrigin // (origins = "http://localhost:8080", allowCredentials = "true")
@RestController
public class ReportController {

	private Database db;
	private SimpleCsvExporterConfiguration config;
	private JRCsvExporter exporter;

	public ReportController() {
		try {
			db = new Database();
			Files.createDirectories(Paths.get(Database.OUTPUT_PATH));
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
			return name.endsWith(".jasper");
		}
	};

	private void createTempIdList(Connection conn, String idList) throws SQLException {
		if (idList != null && idList.indexOf(" ") > 0) {
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

	private String getJasperFile(Map<String, Object> params) {
		String appName = "";
		String dbName = "";
		String jasperFile = (String) params.get("report");
		if (jasperFile != null) {
			if (!jasperFile.contains(".jasper"))
				jasperFile += ".jasper";
		} else {
			jasperFile = "A00.jasper";
		}
		if (params.get("app") != null && !params.get("app").equals(""))
			appName = "-" + params.get("app").toString();
		dbName = (String) params.get("db");
		File jpFile = new File(Database.REPORT_PATH + appName + "/" + dbName + "/" + jasperFile);
		if (!jpFile.exists())
			jpFile = new File(Database.REPORT_PATH + "/" + jasperFile);
		return jpFile.getAbsolutePath();
	}

	private void logClient(HttpServletRequest req, Map<String, Object> params) {
		System.out.println(req.getRemoteAddr() + "_" + req.getMethod() + '_' + (String) params.get("db") + "_"
				+ (String) params.get("report"));
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

	@SuppressWarnings("unchecked")
	@GetMapping(value = "/json", produces = "application/json; charset=UTF-8")
	public ResponseEntity<Map<String, String>[]> json() {
		Map<String, String>[] data = new Map[0];
		File dir = new File(Database.REPORT_PATH);
		if (dir.exists()) {
			String[] list = dir.list(ReportController.filter);
			data = new Map[list.length];
			int i = 0;
			for (String fileName : list)
				try {
					File file = new File(Database.REPORT_PATH + fileName);
					JasperPrint report = JasperFillManager
							.fillReport((JasperReport) JRLoader.loadObjectFromFile(file.getPath()),
									(Map<String, Object>) null, (Connection) null);
					if (report != null) {
						data[i] = new HashMap<>();
						data[i].put("report", file.getName().substring(0, file.getName().indexOf(".jasper")));
						data[i].put("name", report.getName());
						data[i].put("updated", Database.DTFormat.format(file.lastModified()));
						i++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(data);
	}

	@GetMapping(value = "/list", produces = "text/html; charset=UTF-8")
	public String list(HttpServletRequest req) {
		String html = "<html><head><style>table, th, td { border: 1px solid black;} table {width: 100%;}</style></head><body>";
		StringBuilder sb = new StringBuilder(html);
		sb.append("report files in " + Database.REPORT_PATH + "<br>");
		sb.append("session ID: " + req.getSession().getId() + " - cookie: " + req.getCookies() + "<br><br><table>");
		File dir = new File(Database.REPORT_PATH);
		if (dir.exists()) {
			int i = 0;
			String[] list = dir.list(ReportController.filter);
			for (String fileName : list) {
				File file = new File(Database.REPORT_PATH + fileName);
				JasperPrint report = null;
				try {
					report = JasperFillManager
							.fillReport((JasperReport) JRLoader.loadObjectFromFile(file.getPath()),
									(Map<String, Object>) null, (Connection) null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				sb.append("<tr>");
				sb.append("<td>" + (++i) + "</td>");
				sb.append("<td>" + fileName + "</td>");
				if (report != null)
					sb.append("<td>" + report.getName() + "</td>");
				else
					sb.append("<td>NO report</td>");
				sb.append("<td>" + Database.DTFormat.format(file.lastModified()) + "</td>");
				sb.append("</tr>");
			}
		}
		sb.append("</table></body></html>");
		return sb.toString();
	}

	@GetMapping(value = "/getPDF", produces = "application/pdf; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> getPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		return openPDF(request, session, params);
	}

	@PostMapping(value = "/execPDF", produces = "application/pdf; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> execPDF(HttpServletRequest request, HttpSession session,
			@RequestParam Parameter params) {
		return openPDF(request, session, this.mapParams(params));
	}

	@PostMapping(value = "/openPDF", produces = "text/html; charset=UTF-8")
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
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline;")
					.contentType(MediaType.APPLICATION_PDF).body(responseBody);
	}

	@PostMapping(value = "/filePDF", produces = "text/plain")
	public String filePDF(HttpServletRequest request, HttpSession session, @RequestParam Map<String, Object> params) {
		this.logClient(request, params);
		String outputFile = null;
		String sessId = SessionListener.createHashCode(session);
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			outputFile = sessId + "/" + (String) params.get("report") + ".pdf";
			JasperExportManager.exportReportToPdfStream(jasperPrint,
					new FileOutputStream(Database.OUTPUT_PATH + outputFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}

	@PostMapping(value = "/openCSV", produces = "text/csv; charset=UTF-8")
	public ResponseEntity<StreamingResponseBody> openCSV(HttpServletRequest request, HttpSession session,
			@RequestParam Map<String, Object> params) {
		this.logClient(request, params);
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
					System.err.println(e.getMessage());
				}
			};
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		if (responseBody == null)
			return ResponseEntity.notFound().build();
		else
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + params.get("report") + ".csv")
					.body(responseBody);
	}

	@PostMapping(value = "/fileCSV", produces = "text/plain")
	public String fileCSV(HttpServletRequest request, HttpSession session, @RequestParam Map<String, Object> params) {
		this.logClient(request, params);
		String outputFile = null;
		String sessId = SessionListener.createHashCode(session);
		try {
			JasperPrint jasperPrint = loadJasperFile(params);
			outputFile = sessId + "/" + (String) params.get("report") + ".csv";
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(
					new SimpleWriterExporterOutput(new FileOutputStream(Database.OUTPUT_PATH + outputFile)));
			exporter.setConfiguration(this.config);
			exporter.exportReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFile;
	}
}