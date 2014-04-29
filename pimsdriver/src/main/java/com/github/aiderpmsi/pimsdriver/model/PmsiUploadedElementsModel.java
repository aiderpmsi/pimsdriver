package com.github.aiderpmsi.pimsdriver.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PmsiUploadedElementsModel {

	private List<PmsiUploadedElementModel> elements;

	public List<PmsiUploadedElementModel> getElements() {
		return elements;
	}

	public void setElements(List<PmsiUploadedElementModel> elements) {
		this.elements = elements;
	}

}
