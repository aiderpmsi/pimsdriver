package com.github.aiderpmsi.pimsdriver.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.servlet.http.Part;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

@Named("forms.rsfUploadFormModel")
@ConversationScoped
public class RsfUploadFormModel extends SimpleRsfUploadForm implements Serializable {

	/**
	 * Generated serialid
	 */
	private static final long serialVersionUID = -4832734999690127718L;
	
	/**
	 * Rsf File. Must be not null
	 */
	@NotNull
	@Size(min=1)
	private Part rsfFile;

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public RsfUploadFormModel() {
		super();
	}

	/**
	 * Processes the form : Stores in database the upload. The processing will
	 * occure later
	 * 
	 * @return redirection
	 * @throws IOException 
	 */
	public String process() throws IOException {

		// SAVES THE INFORMATIONS IN THE DB
		ODatabaseDocumentTx tx = DocDbConnectionFactory.getConnection();
		try {
			// DECLARE THE CLASSES WE WILL USE
			if (!tx.getMetadata().getSchema().existsClass("PmsiUpload"))
				tx.getMetadata().getSchema().createClass("PmsiUpload");

			// TX
			tx.begin();

			ODocument odoc = tx.newInstance("PmsiUpload");
			(new PimsODocumentHelper(odoc)).field("rsf",
					rsfFile.getInputStream());
			odoc.field("dateEnvoi", (new Date()).getTime());
			odoc.field("monthValue", getMonthValue());
			odoc.field("yearValue", getYearValue());
			odoc.field("finessValue", getFinessValue());
			odoc.field("processed", false);
			tx.save(odoc);
			
			tx.commit();

		} finally {
			tx.close();
		}

		// If Form validation passed, 
		return "/importchoice.xhtml?faces-redirect=true";
	}

	public Part getFile() {
		return rsfFile;
	}

	public void setFile(Part file) {
		this.rsfFile = file;
	}
	
}
