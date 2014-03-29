package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="rsfrssimport")
public class ImportRsfRssModel extends ImportRsfModel {

	/**
	 * Rss File. Must be not null
	 */
	@NotNull
	private String rss;

	public String getRss() {
		return rss;
	}

	public void setRss(String rss) {
		this.rss = rss;
	}

}
