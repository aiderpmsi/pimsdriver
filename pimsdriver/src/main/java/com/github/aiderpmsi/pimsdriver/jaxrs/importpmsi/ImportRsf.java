package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.PermitAll;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.views.VoidElement;

@Path("/import") 
@PermitAll
public class ImportRsf {
	
	@GET
    @Path("/welcome")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importwelcome.xslt\"?>")
    public VoidElement welcome() {
		return new VoidElement();
    }

	@GET
    @Path("/singlersf")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/importsinglersf.xslt\"?>")
    public ImportRsfModel singlersfGet(
    		@QueryParam("month") Integer month,
    		@QueryParam("year") Integer year,
    		@QueryParam("finess") String finess,
    		@QueryParam("file") String filePath) {

		// CREATES THE MODEL
		ImportRsfModel model = new ImportRsfModel();
		if (month != null) model.setMonthValue(month);
		if (year != null) model.setYearValue(year);
		if (finess != null) model.setFinessValue(finess);
		if (filePath != null) model.setFile(filePath);
		
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

	@POST
    @Path("/singlersf")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response singlersfPost(
    		@FormDataParam("month") Integer month,
    		@FormDataParam("year") Integer year,
    		@FormDataParam("finess") String finess,
    		@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader ) {
		
		// CREATES THE MODEL
		ImportRsfModel model = new ImportRsfModel();
		if (month != null) model.setMonthValue(month);
		if (year != null) model.setYearValue(year);
		if (finess != null) model.setFinessValue(finess);
		if (contentDispositionHeader != null) model.setFile(contentDispositionHeader.getFileName());
		
		// VALIDATES THE MODEL
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<ImportRsfModel>> constraintViolations =
				validator.validate(model);
		
		
		// IF ERROR IN THE VALIDATION, REDIRECTS TO FORM
		if (constraintViolations.size() != 0) {
			URI redirection = UriBuilder.fromUri("")
				    .path("singlersf")
				    .queryParam("month", month)
				    .queryParam("year", year)
				    .queryParam("finess", finess)
				    .queryParam("file", contentDispositionHeader.getFileName())
				    .build();
			ResponseBuilder resp = Response.seeOther(redirection);
			return resp.build();
		} else {
			URI redirection = UriBuilder.fromUri("")
				    .path("ok").build();
			ResponseBuilder resp = Response.seeOther(redirection);
			return resp.build();
		}
    }
}
