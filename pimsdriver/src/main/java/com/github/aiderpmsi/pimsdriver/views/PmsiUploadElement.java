package com.github.aiderpmsi.pimsdriver.views;

import java.util.Date;

import com.orientechnologies.orient.core.id.ORID;

public class PmsiUploadElement {

	private ORID recordId;
	
	private Date dateEnvoi;

	private Integer monthValue;
	
	private Integer yearValue;
	
	private String finessValue;
	
	private Boolean processed;

	public Date getDateEnvoi() {
		return dateEnvoi;
	}

	public void setDateEnvoi(Date dateEnvoi) {
		this.dateEnvoi = dateEnvoi;
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

	public Boolean getProcessed() {
		return processed;
	}

	public void setProcessed(Boolean processed) {
		this.processed = processed;
	}

	public ORID getRecordId() {
		return recordId;
	}

	public void setRecordId(ORID recordId) {
		this.recordId = recordId;
	}

}
