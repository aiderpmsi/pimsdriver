package com.github.aiderpmsi.pimsdriver.views;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Named("views.pmsiUploadsBean")
@ConversationScoped
public class PmsiUploadsBean implements Serializable {

	/**
	 * Generated serialid
	 */
	private static final long serialVersionUID = 712160288095075205L;

	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	@NotNull
	private String filter;
	
	@Min(0)
	@NotNull
	private Integer first;
	
	@Min(0)
	@NotNull
	private Integer numrows;
	
	private Boolean lastRow = true;
	
	public void validate() throws IllegalArgumentException {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<PmsiUploadsBean>> constraintViolations =
				validator.validate(this);

		if (constraintViolations.size() > 0 ) {
			throw new IllegalArgumentException(constraintViolations.toString());
		}
	}
	
	public List<PmsiUploadElement> getElements() {
		List<PmsiUploadElement> data = new ArrayList<PmsiUploadElement>(numrows);

		// Create Query
		StringBuilder query = new StringBuilder("select * from PmsiUpload ");

		if (!filter.equals("all")) {
			query.append("where ");
			if (filter.equals("notprocessed")) {
				query.append("processed=false ");
			} else if (filter.equals("processed")) {
				query.append("processed=true ");
			}
		}

		query.append("offset ").append(first).append(" limit ").append(numrows + 1);

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(
				query.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getConnection();
			tx.begin();
			results = tx.command(oquery).execute();
			tx.commit();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		} finally {
			if (tx != null)
				tx.close();
		}

		// Creates The elements for max numrows
		int i = 0;
		for (ODocument result : results) {
			if (i > numrows) {
				setLastRow(false);
				break;
			}
			PmsiUploadElement element = new PmsiUploadElement();
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi(new Date((Long) result.field("dateEnvoi")));
			element.setFinessValue((String) result.field("finessValue"));
			element.setMonthValue((Integer) result.field("monthValue"));
			element.setProcessed((Boolean) result.field("processed"));
			element.setYearValue((Integer) result.field("yearValue"));
			if (result.field("rss") == null)
				element.setComment("RSF");
			else
				element.setComment("RSF et RSS");
			data.add(element);
			i++;
		}

		return data;
	}
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Integer getFirst() {
		return first;
	}

	public void setFirst(Integer first) {
		this.first = first;
	}

	public Integer getNumrows() {
		return numrows;
	}

	public void setNumrows(Integer numrows) {
		this.numrows = numrows;
	}

	public Boolean getLastRow() {
		return lastRow;
	}

	protected void setLastRow(Boolean lastRow) {
		this.lastRow = lastRow;
	}

}
