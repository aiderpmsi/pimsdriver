package com.github.aiderpmsi.pimsdriver.dao.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlValue;

/**
 * Represents a list of uploaded pmsi
 * @author jpc
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UploadedPmsis {

	private List<UploadedPmsi> elements;
	
	@XmlValue
	@XmlElementWrapper(name="uploadedelements")
	@XmlElements({
		@XmlElement(name="uploadedelement", type=UploadedPmsi.class)
	})
	public List<UploadedPmsi> getElements() {
		return elements;
	}

	public void setElements(List<UploadedPmsi> elements) {
		this.elements = elements;
	}

}
