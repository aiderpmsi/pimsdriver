package com.github.aiderpmsi.pimsdriver.odb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

public class ORIDJaxbAdapter extends XmlAdapter<String, ORID> {

	@Override
	public ORID unmarshal(String v) throws Exception {
		return new ORecordId(v);
	}

	@Override
	public String marshal(ORID v) throws Exception {
		return v.toString();
	}
	
}
