package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.github.aiderpmsi.pimsdriver.odb.ORIDJaxbAdapter;
import com.orientechnologies.orient.core.id.ORID;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class UploadedElement {

	private ORID recordId;
	
	private Long rowNumber;
	
	private Date dateEnvoi;

	private Integer month;
	
	private Integer year;
	
	private String finess;
	
	private Boolean processed;
	
	private Boolean success;
	
	private String comment;

	@XmlJavaTypeAdapter(ORIDJaxbAdapter.class)
	public ORID getRecordId() {
		return recordId;
	}

	public void setRecordId(ORID recordId) {
		this.recordId = recordId;
	} 

	public Long getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Long rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Date getDateEnvoi() {
		return dateEnvoi;
	}

	public void setDateEnvoi(Date dateEnvoi) {
		this.dateEnvoi = dateEnvoi;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getFiness() {
		return finess;
	}

	public void setFiness(String finess) {
		this.finess = finess;
	}

	public Boolean getProcessed() {
		return processed;
	}

	public void setProcessed(Boolean processed) {
		this.processed = processed;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
