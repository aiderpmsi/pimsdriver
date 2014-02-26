package com.github.aiderpmsi.pimsdriver.odb;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

public class ORIDJaxbAdapter extends XmlAdapter<String, ORID> {
	
	private Pattern pattern = Pattern.compile("#?([+-]?[0-9]+):([+-]?[0-9]+)");
	
	@Override
	public ORID unmarshal(String v) throws Exception {
		Matcher m = pattern.matcher(v.toString());
		String ret = "#" + m.group(1) + ":" + m.group(2);
		return new ORecordId(ret);
	}

	@Override
	public String marshal(ORID v) throws Exception {
		Matcher m = pattern.matcher(v.toString());
		if (m.matches()) {
			String ret = m.group(1) + ":" + m.group(2);
			return ret;
		} else {
			throw new IOException("Unable to marshal " + v.toString());
		}
	}
	
}
