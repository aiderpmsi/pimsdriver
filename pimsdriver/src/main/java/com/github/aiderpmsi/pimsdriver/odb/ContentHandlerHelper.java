package com.github.aiderpmsi.pimsdriver.odb;

import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class ContentHandlerHelper implements ContentHandler {

	private LinkedList<String> contentPath = new LinkedList<String>();
	
	private String cachedPath = null;

	public String getPath() {
		if (cachedPath == null) {
			cachedPath = "/" + StringUtils.join(contentPath, "/");
		}
		return cachedPath;
	}
	
	public LinkedList<String> getContentPath() {
		return contentPath;
	}

	public void setContentPath(LinkedList<String> contentPath) {
		this.contentPath = contentPath;
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		// REMOVE THE LAST ELEMENT
		contentPath.removeLast();
		// REINIT THE PATH
		cachedPath = null;
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		// ADDS THE ELEMENT
		contentPath.addLast(arg1);
		// REINIT THE PATH
		cachedPath = null;
	}

}
