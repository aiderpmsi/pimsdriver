package com.github.aiderpmsi.pimsdriver.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PmsiUploadedElementModel extends PmsiUploadElementModel {

	public enum Status {pending, successed, failed};
	
	/** Primary key */
	private Long recordId;
	
	/** Date of data upload */
	private Date dateenvoi;
	
	/** Status of this upload */
	private Status processed;

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Date getDateenvoi() {
		return dateenvoi;
	}

	public void setDateenvoi(Date dateenvoi) {
		this.dateenvoi = dateenvoi;
	}

	public Status getProcessed() {
		return processed;
	}

	public void setProcessed(Status processed) {
		this.processed = processed;
	}
	
	
}
