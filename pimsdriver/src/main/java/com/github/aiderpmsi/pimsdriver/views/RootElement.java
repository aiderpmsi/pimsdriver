package com.github.aiderpmsi.pimsdriver.views;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RootElement {

	@XmlElement
	private String element = "Test";

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}
}
