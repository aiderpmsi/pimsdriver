package com.github.aiderpmsi.pimsdriver.dto.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a list of uploaded pmsi
 * @author jpc
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class UploadedPmsis {

	private List<UploadedPmsi> elements;
	
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
