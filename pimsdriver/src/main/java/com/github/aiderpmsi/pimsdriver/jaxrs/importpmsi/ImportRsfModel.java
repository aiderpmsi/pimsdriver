package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="rsfimport")
public class ImportRsfModel extends ImportPmsiBaseModel {

	/**
	 * Rsf File. Must be not null
	 */
	@NotNull
	@Size(min=1)
	private String file = "";

	/**
	 * Creates the Form with default values : - month = current month - year =
	 * current year
	 */
	public ImportRsfModel() {
		super();
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
}
