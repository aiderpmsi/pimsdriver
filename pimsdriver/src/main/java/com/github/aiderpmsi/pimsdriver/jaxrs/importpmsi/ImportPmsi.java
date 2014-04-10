package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.jaxrs.HtmlHelper;
import com.github.aiderpmsi.pimsdriver.jaxrs.VoidElement;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

@Path("/import") 
@PermitAll
public class ImportPmsi {
	
	@GET
    @Path("/welcome")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importwelcome.xslt\"?>")
    public VoidElement welcomeXml() {
		return new VoidElement();
    }

	@GET
    @Path("/welcome")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput welcomeHtml(
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(welcomeXml())
			.setXslResource("importwelcome");

		return help;
    }
	
	@GET
    @Path("/singlersf")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importsinglersf.xslt\"?>")
    public ImportRsfModel singlersfGetXml(
    		@QueryParam("month") Integer month,
    		@QueryParam("year") Integer year,
    		@QueryParam("finess") String finess,
    		@QueryParam("rsf") String fileName) {

		// CREATES THE MODEL
		ImportRsfModel model = new ImportRsfModel();
		
		// IF NO PARAMETER IS ENTERED, CREATE A DEFAULT MODEL, AND DO NOT VALIDATE
		if (month == null && year == null && finess == null && fileName == null) {
			model.setDefaultValues();
			return model;
		}

		// IF SOME PARAMETERS ARE ALREADY ENTERED, VALIDATE THOSE PARAMETERS
		if (month != null) model.setMonthValue(month);
		if (year != null) model.setYearValue(year);
		if (finess != null) model.setFinessValue(finess);
		if (fileName != null) model.setRsf(fileName);
		
		// VALIDATES THE MODEL
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ImportRsfModel>> constraintViolations =
				validator.validate(model);
		
		// GENERATES THE ERRORS (IF ANY) IN FORM MODEL
		Map<String, String> violations = new HashMap<String, String>();
		for (ConstraintViolation<ImportRsfModel> violation : constraintViolations) {
			violations.put(violation.getPropertyPath().toString(),
					violation.getMessage());
		}
		model.setErrorsModel(violations);
		
        return model;
    }

	@GET
    @Path("/singlersf")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput singlersfGetHtml(
    		@QueryParam("month") Integer month,
    		@QueryParam("year") Integer year,
    		@QueryParam("finess") String finess,
    		@QueryParam("rsf") String fileName,
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(singlersfGetXml(month, year, finess, fileName))
			.setXslResource("importsinglersf");

		return help;
    }
	
	@POST
    @Path("/singlersf")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response singlersfPost(
    		@FormDataParam("month") Integer month,
    		@FormDataParam("year") Integer year,
    		@FormDataParam("finess") String finess,
    		@FormDataParam("rsf") InputStream rsf,
			@FormDataParam("rsf") FormDataContentDisposition rsfInformations,
			@Context UriInfo uriInfo) throws IOException {
		
		// CREATES THE MODEL
		ImportRsfModel model = new ImportRsfModel();
		model.setMonthValue(month);
		model.setYearValue(year);
		model.setFinessValue(finess);
		if (rsfInformations != null) model.setRsf(rsfInformations.getFileName());
		
		// VALIDATES THE MODEL
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ImportRsfModel>> constraintViolations =
				validator.validate(model);
		
		if (constraintViolations.size() != 0) {
			// IF ERROR IN THE VALIDATION, REDIRECTS TO FORM
			UriBuilder redirectionBuilder = uriInfo.getBaseUriBuilder().
					path(ImportPmsi.class).
					path(ImportPmsi.class, "singlersfGetXml");
			if (month != null) redirectionBuilder.queryParam("month", month);
			if (year != null) redirectionBuilder.queryParam("year", year);
			if (finess != null) redirectionBuilder.queryParam("finess", finess);
			if (rsfInformations != null) redirectionBuilder.queryParam("rsf", rsfInformations.getFileName());
			ResponseBuilder resp = Response.seeOther(redirectionBuilder.build());
			return resp.build();
		} else {
			// IF NO ERROR IN THE FORM, STORE THE FORM CONTENT IN THE DATABASE
			importRsf(model, rsf);
			
			// REDIRECT TO OK WINDOW
			UriBuilder redirectionBuilder = uriInfo.getBaseUriBuilder().
					path(ImportPmsi.class).
					path(ImportPmsi.class, "singleRsfOkXml");
			ResponseBuilder resp = Response.seeOther(redirectionBuilder.build());
			return resp.build();
		}
    }
	
	public void importRsf(ImportPmsiBaseModel model, InputStream rsf) throws IOException {
		ODatabaseDocumentTx db = DocDbConnectionFactory.getInstance().getConnection();
		
		try {
			// TX BEGIN
			db.begin();
			Date now = new Date();
			// CREATES THE ENTRY IN THE RIGHT CLASS
			ODocument odoc = db.newInstance("PmsiUpload");
			// HERLPER FOR THIS DOCUMENT (STORE FILE)
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);
			odocHelper.field("rsf", rsf);
			odoc.field("month", model.getMonthValue());
			odoc.field("year", model.getYearValue());
			odoc.field("finess", model.getFinessValue());
			odoc.field("processed", "pending");
			odoc.field("dateenvoi", now);
			// SAVE THIS ENTRY
			db.save(odoc);
			// TX END
			db.commit();
		} finally {
			db.close();
		}

	}
	
	@GET
	@Path("/singlersfok")
	@Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importsinglersfok.xslt\"?>")
	public VoidElement singleRsfOkXml() {
		return new VoidElement();
	}
	
	@GET
    @Path("/singlersfok")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput singleRsfOkHtml(
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(singleRsfOkXml())
			.setXslResource("importsinglersfok");

		return help;
    }

	@GET
    @Path("/rsfrss")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importrsfrss.xslt\"?>")
    public ImportRsfModel rsfRssGetXml(
    		@QueryParam("month") Integer month,
    		@QueryParam("year") Integer year,
    		@QueryParam("finess") String finess,
    		@QueryParam("rsf") String rsfFileName,
    		@QueryParam("rss") String rssFileName) {

		// CREATES THE MODEL
		ImportRsfRssModel model = new ImportRsfRssModel();
		
		// IF NO PARAMETER IS ENTERED, CREATE A DEFAULT MODEL, AND DO NOT VALIDATE
		if (month == null && year == null && finess == null && rsfFileName == null && rssFileName == null) {
			model.setDefaultValues();
			return model;
		}

		// IF SOME PARAMETERS ARE ALREADY ENTERED, VALIDATE THOSE PARAMETERS
		if (month != null) model.setMonthValue(month);
		if (year != null) model.setYearValue(year);
		if (finess != null) model.setFinessValue(finess);
		if (rsfFileName != null) model.setRsf(rsfFileName);
		if (rssFileName != null) model.setRss(rssFileName);
		
		// VALIDATES THE MODEL
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ImportRsfRssModel>> constraintViolations =
				validator.validate(model);
		
		// GENERATES THE ERRORS (IF ANY) IN FORM MODEL
		Map<String, String> violations = new HashMap<String, String>();
		for (ConstraintViolation<ImportRsfRssModel> violation : constraintViolations) {
			violations.put(violation.getPropertyPath().toString(),
					violation.getMessage());
		}
		model.setErrorsModel(violations);
		
        return model;
    }

	@GET
    @Path("/rsfrss")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput rsfRssGetHtml(
    		@QueryParam("month") Integer month,
    		@QueryParam("year") Integer year,
    		@QueryParam("finess") String finess,
    		@QueryParam("rsf") String rsfFileName,
    		@QueryParam("rss") String rssFileName,
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(rsfRssGetXml(month, year, finess, rsfFileName, rssFileName))
			.setXslResource("importrsfrss");

		return help;
    }
	
	@POST
    @Path("/rsfrss")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response rsfRssPost(
    		@FormDataParam("month") Integer month,
    		@FormDataParam("year") Integer year,
    		@FormDataParam("finess") String finess,
    		@FormDataParam("rsf") InputStream rsf,
			@FormDataParam("rsf") FormDataContentDisposition rsfInformations,
    		@FormDataParam("rss") InputStream rss,
			@FormDataParam("rss") FormDataContentDisposition rssInformations,
			@Context UriInfo uriInfo) throws IOException {
		
		// CREATES THE MODEL
		ImportRsfRssModel model = new ImportRsfRssModel();
		model.setMonthValue(month);
		model.setYearValue(year);
		model.setFinessValue(finess);
		if (rsfInformations != null) model.setRsf(rsfInformations.getFileName());
		if (rssInformations != null) model.setRss(rssInformations.getFileName());
		
		// VALIDATES THE MODEL
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ImportRsfRssModel>> constraintViolations =
				validator.validate(model);
		
		if (constraintViolations.size() != 0) {
			// IF ERROR IN THE VALIDATION, REDIRECTS TO FORM
			UriBuilder redirectionBuilder = uriInfo.getBaseUriBuilder().
					path(ImportPmsi.class).
					path(ImportPmsi.class, "rsfRssGetXml");
			if (month != null) redirectionBuilder.queryParam("month", month);
			if (year != null) redirectionBuilder.queryParam("year", year);
			if (finess != null) redirectionBuilder.queryParam("finess", finess);
			if (rsfInformations != null) redirectionBuilder.queryParam("rsf", rsfInformations.getFileName());
			if (rssInformations != null) redirectionBuilder.queryParam("rss", rssInformations.getFileName());
			ResponseBuilder resp = Response.seeOther(redirectionBuilder.build());
			return resp.build();
		} else {
			// IF NO ERROR IN THE FORM, STORE THE FORM CONTENT IN THE DATABASE
			importRsfRss(model, rsf, rss);

			// REDIRECT TO OK WINDOW
			UriBuilder redirectionBuilder = uriInfo.getBaseUriBuilder().
					path(ImportPmsi.class).
					path(ImportPmsi.class, "rsfRssOkXml");
			ResponseBuilder resp = Response.seeOther(redirectionBuilder.build());
			return resp.build();
		}
    }

	public void importRsfRss(ImportPmsiBaseModel model, InputStream rsf, InputStream rss) throws IOException {
		ODatabaseDocumentTx db = DocDbConnectionFactory.getInstance().getConnection();
		
		try {
			// TX BEGIN
			db.begin();
			Date now = new Date();
			// CREATES THE ENTRY IN THE RIGHT CLASS
			ODocument odoc = db.newInstance("PmsiUpload");
			// HERLPER FOR THIS DOCUMENT (STORE FILE)
			PimsODocumentHelper odocHelper = new PimsODocumentHelper(odoc);
			odocHelper.field("rsf", rsf);
			odocHelper.field("rss", rss);
			odoc.field("month", model.getMonthValue());
			odoc.field("year", model.getYearValue());
			odoc.field("finess", model.getFinessValue());
			odoc.field("processed", "pending");
			odoc.field("dateenvoi", now);
			// SAVE THIS ENTRY
			db.save(odoc);
			// TX END
			db.commit();
		} finally {
			db.close();
		}

	}

	@GET
	@Path("/rsfrssok")
	@Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importrsfrssok.xslt\"?>")
	public VoidElement rsfRssOkXml() {
		return new VoidElement();
	}
	
	@GET
    @Path("/rsfrssok")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput rsfRssOkHtml(
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(rsfRssOkXml())
			.setXslResource("importrsfrssok");

		return help;
    }

}
