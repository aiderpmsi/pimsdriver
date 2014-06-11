package com.github.aiderpmsi.pimsdriver.dto.model;

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
	@XmlElement
	public Long recordid;
	
	/** Date of data upload */
	@XmlElement
	public Date dateenvoi;
	
	/** Status of this upload */
	@XmlElement
	public Status processed;
	
	/** ID of the RSF OID */
	public Long rsfoid;
	
	/** ID of the RSS OID */
	public Long rssoid;

	/** Attributes */
	@XmlElement
	public HashMap<String, String> attributes;

	
}
