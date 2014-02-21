package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="uploaded")
@XmlSeeAlso({UploadedElement.class})
public class UploadedElements {

	private List<UploadedElement> elements;

	private Boolean lastChunk;
	
	@XmlElementWrapper
	public List<UploadedElement> getElements() {
		return elements;
	}

	public void setElements(List<UploadedElement> elements) {
		this.elements = elements;
	}

	public Boolean getLastChunk() {
		return lastChunk;
	}

	public void setLastChunk(Boolean lastChunk) {
		this.lastChunk = lastChunk;
	}
	
}
