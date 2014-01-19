package com.github.aiderpmsi.pimsdriver.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.servlet.http.Part;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.PimsODocumentHelper;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

@Named("forms.rsfUploadFormModel")
@ConversationScoped
public class RsfUploadFormModel implements Serializable {

	private static final long serialVersionUID = 2979279769871276762L;

	/**
	 * Rsf File. Must be not null
	 */
	@NotNull
	private Part rsfFile;

	/**
	 * Pmsi Month. Must be between 1 and 12
	 */
	@Min(1)
	@Max(12)
	private Integer monthValue;

	/**
	 * Pmsi Year. Must be non null
	 */
	@NotNull
	private Integer yearValue;

	/**
	 * Finess Value. Must be non null
	 */
	@NotNull
	private String finessValue;

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public RsfUploadFormModel() {
		// Gets the current Calendar (Gregorian calendar)
		Calendar cal = GregorianCalendar.getInstance();

		// Sets the current month and current year
		setMonthValue(cal.get(Calendar.MONTH) + 1);
		setYearValue(cal.get(Calendar.YEAR));
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

	public Integer getMonthValue() {
		return monthValue;
	}

	public void setMonthValue(Integer monthValue) {
		this.monthValue = monthValue;
	}

	public Integer getYearValue() {
		return yearValue;
	}

	public void setYearValue(Integer yearValue) {
		this.yearValue = yearValue;
	}

	public String getFinessValue() {
		return finessValue;
	}

	public void setFinessValue(String finessValue) {
		this.finessValue = finessValue;
	}

}
