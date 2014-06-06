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

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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

@Path("/report") 
@PermitAll
public class Report {
	
	private static final String rootResource = "com/github/aiderpmsi/pimsdriver/jreport/";

	@GET
    @Path("/report/{id}/factures{finess}.pdf")
    @Produces({"application/pdf"})
	public Response getPendingUploadedElements(
			@PathParam("id") final Long id,
			@PathParam("finess") String finess) {
		
		StreamingOutput stream = new StreamingOutput() {
	
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
			
				OutputStream bos = new BufferedOutputStream(os);
				try {
					JasperReport report = reportFromResourceName("fact_main.jrxml");

					Map<String, Object> parametres = new HashMap<>();

					parametres.put("plud_id", id);
					
					Connection con = null;
					try {
						con = DataSourceSingleton.getInstance().getConnection();
					
						JasperPrint print = JasperFillManager.fillReport(report, parametres, con);
						JasperExportManager.exportReportToPdfStream(print, bos);
						
						con.commit();
					} finally {
						if (con != null) con.close();
					}
				} catch (JRException | SQLException e) {
					throw new IOException(e);
				} finally {
					bos.flush();
				}
				return;
			}
		};
		
		return Response.ok(stream).build();
	}

	public static JasperReport reportFromResourceName(String resource) throws IOException {
		InputStream is = null;
		try {
			is = new BufferedInputStream(Report.class.getClassLoader().getResourceAsStream(rootResource + resource));
			JasperDesign design = JRXmlLoader.load(is);
			JasperReport report = JasperCompileManager.compileReport(design);
			return report;
		} catch (JRException e) {
			throw new IOException(e);
		} finally {
			if (is != null) is.close();
		}
	}
	
}
