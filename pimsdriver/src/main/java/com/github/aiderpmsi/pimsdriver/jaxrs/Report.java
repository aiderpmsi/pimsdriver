package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.report.CacheHashMap;

@Path("/report") 
@PermitAll
public class Report {
	
	private static final String rootResource = "com/github/aiderpmsi/pimsdriver/jreport/";
	
	private static final CacheHashMap<String, JasperReport> reports = new CacheHashMap<>(20);

	private static final ReentrantLock reportsLock = new ReentrantLock(false);
	
	@GET
    @Path("/report/{id}/factures.pdf")
    @Produces({"application/pdf"})
	public Response getFactures(
			@PathParam("id") final Long id,
			@Context ServletContext context) {
		
		StreamingOutput stream = new StreamingOutput() {
	
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
			
				try (
						OutputStream bos = new BufferedOutputStream(os);
						Connection con = DataSourceSingleton.getConnection(context);) {

					JasperReport report = reportFromResourceName("fact_main.jrxml");

					Map<String, Object> parametres = new HashMap<>();

					parametres.put("plud_id", id);

					JasperPrint print = JasperFillManager.fillReport(report, parametres, con);
					JasperExportManager.exportReportToPdfStream(print, bos);
						
					con.commit();
				} catch (SQLException | JRException e) {
					throw new IOException(e);
				}
			}
		};
		
		return Response.ok(stream).build();
	}

	@GET
    @Path("/report/{id}/sejours.pdf")
    @Produces({"application/pdf"})
	public Response getSejours(
			@PathParam("id") final Long id,
			@Context ServletContext context) {
		
		StreamingOutput stream = new StreamingOutput() {
	
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
			
				try (
						OutputStream bos = new BufferedOutputStream(os);
						Connection con = DataSourceSingleton.getConnection(context);) {

					JasperReport report = reportFromResourceName("sejour_main.jrxml");

					Map<String, Object> parametres = new HashMap<>();

					parametres.put("plud_id", id);

					JasperPrint print = JasperFillManager.fillReport(report, parametres, con);
					JasperExportManager.exportReportToPdfStream(print, bos);
						
					con.commit();
				} catch (SQLException | JRException e) {
					throw new IOException(e);
				}
			}
		};
		
		return Response.ok(stream).build();
	}

	public static JasperReport reportFromResourceName(String resource) throws IOException {
		JasperReport report;

		// IF REPORT EXISTS, RETURN IT
		try {
			reportsLock.lock();
			if ((report = reports.get(resource)) != null)
				return report;
		} finally {
			reportsLock.unlock();
		}
		
		// IF REPORT DOESN'T EXIST, CREATE IT
		try (InputStream is = new BufferedInputStream(Report.class.getClassLoader().getResourceAsStream(rootResource + resource))) {
			
			JasperDesign design = JRXmlLoader.load(is);
			report = JasperCompileManager.compileReport(design);
			// IF REPORT HAS BEEN INSERTED FROM ANOTHER THREAD, DO NOT STORE IT
			try {
				reportsLock.lock();
				if (!reports.containsKey(resource))
					reports.put(resource, report);
			} finally {
				reportsLock.unlock();
			}
			return report;
		} catch (JRException e) {
			throw new IOException(e);
		}
	}
	
}
