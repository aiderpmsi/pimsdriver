package com.github.aiderpmsi.pimsdriver.views;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="rootelement")
public class WelcomeElement {

	@XmlElement(name="element")
	private String element = "Test";

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}
}
