package com.github.aiderpmsi.pimsdriver.dao.model;

import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents an upload after the upload (with some more items set than PmsiUploadElementmodel)
 * @author jpc
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
public class UploadedPmsi extends UploadPmsi {

	public enum Status {pending, successed, failed};
	
	/** Primary key */
	private Long recordid;
	
	/** Date of data upload */
	private Date dateenvoi;
	
	/** Status of this upload */
	private Status processed;
	
	/** ID of the RSF OID */
	private Long rsfoid;
	
	/** ID of the RSS OID */
	private Long rssoid;

	/** Attributes */
	private HashMap<String, String> attributes;

	@XmlElement
	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	@XmlElement
	public Date getDateenvoi() {
		return dateenvoi;
	}

	public void setDateenvoi(Date dateenvoi) {
		this.dateenvoi = dateenvoi;
	}

	@XmlElement
	public Status getProcessed() {
		return processed;
	}

	public void setProcessed(Status processed) {
		this.processed = processed;
	}

	public Long getRsfoid() {
		return rsfoid;
	}

	public void setRsfoid(Long rsfoid) {
		this.rsfoid = rsfoid;
	}

	public Long getRssoid() {
		return rssoid;
	}

	public void setRssoid(Long rssoid) {
		this.rssoid = rssoid;
	}

	@XmlElement
	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}
	
	
}
