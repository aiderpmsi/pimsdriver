package com.github.aiderpmsi.pimsdriver;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.servlet.http.Part;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Named("bean")
@ConversationScoped
public class Bean implements Serializable {

	private static final long serialVersionUID = 2979279769871276762L;

	private Part file;

	private Integer monthValue;

	private Integer yearValue;
	
	private String finessValue;

	public Bean() {
		setMonthValue(1);
		setYearValue(2013);
	}

	public String upload() {
		try {
			// SAVES THE INFORMATIONS IN THE DB
			ODatabaseDocumentTx tx = DocDbConnectionFactory.getConnection();
			try {
				// DECLARE THE CLASSES WE WILL USE
				if (!tx.getMetadata().getSchema().existsClass("PmsiUpload"))
					tx.getMetadata().getSchema().createClass("PmsiUpload");
				
				// TX
				tx.begin();
				ODocument odoc = tx.newInstance("test");
				(new PimsODocumentHelper(odoc)).field("rsf",
						file.getInputStream());
				odoc.field("monthValue", getMonthValue());
				odoc.field("yearValue", getYearValue());
				odoc.field("finessValue", getFinessValue());
				odoc.field("processed", false);
				tx.save(odoc);
				tx.commit();

				List<ODocument> result = tx
						.query(new OSQLSynchQuery<ODocument>(
								"select * from test where Month='12'"));
				for (ODocument d : result) {
					System.out.println("Record : Month = " + d.field("Month"));
				}
			} finally {
				tx.close();
			}
			// ContentHandler ch = new DefaultContentHandler(new PrintWriter(new
			// File("/home/AIDER-delabre/test_output.xml")));
			// Parser pa = new Parser(new
			// InputStreamReader(file.getInputStream(), "UTF-8"), ch);
			// pa.parse();

		} catch (IOException e) {
			// Nothing
		}
		return "importchoice?faces-redirect=true";
	}

	public Part getFile() {
		return file;
	}

	public void setFile(Part file) {
		this.file = file;
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
