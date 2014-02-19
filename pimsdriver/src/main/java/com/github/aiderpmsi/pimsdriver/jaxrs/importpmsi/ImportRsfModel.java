package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="rsfimport")
public class ImportRsfModel extends ImportPmsiBaseModel {

	/**
	 * Rsf File. Must be not null
	 */
	@NotNull
	private String rsf;

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public ImportRsfModel() {
		super();
	}

	public String getRsf() {
		return rsf;
	}

	public void setRsf(String rsf) {
		this.rsf = rsf;
	}

}
