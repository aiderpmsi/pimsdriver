package com.github.aiderpmsi.pimsdriver.db;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;

import org.xml.sax.SAXException;

public class RssContentHandler extends PmsiContentHandlerHelper {

	protected StringBuilder propertyContent = new StringBuilder();
	
	protected HashMap<String, String> propertyValues = new HashMap<>();
	
	protected GroupDbLink groupdblink;
	
	public RssContentHandler(Connection con, Long uploadPKId, long pmsiPosition) throws IOException {
		groupdblink = new GroupDbLink();
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN ELEMENT, SEND IT TO THE GROUP PROCESSING
		if (position == State.ELEMENT) {
			GroupEntry entry = new GroupEntry();
			entry.content = propertyValues;
			entry.line_type = contentPath.getLast();
			try {
				groupdblink.store(entry);
			} catch (InterruptedException e) {
				throw new SAXException(e);
			}
			// AND THEN RESET VALUES
			propertyValues = new HashMap<>();
		} else if (position == State.PROPERTY) {
			// IF WE ARE LEAVING A PROPERTY, STORE IT
			propertyValues.put(contentPath.getLast(), propertyContent.toString());
			propertyContent = new StringBuilder();
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE IN PROPERTY OF A STANDARD ELEMENT, SAVE IT IN PROPERTYCONTENT
		if (position == State.PROPERTY) {
			propertyContent.append(ch, start, length);
		}
		super.characters(ch, start, length);
	}

	@Override
	public String[][] getNumLinePath() {
		return numLinePath;
	}

	@Override
	public String[][] getElementPath() {
		return elementPath;
	}

	@Override
	public String[][] getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String[][] getHeaderPath() {
		return headerPath;
	}

	private static final String[][] numLinePath = {{"root"}, {"numline"}};

	private static final String[][] headerPath = {{"root"}, {"rssheader"}};

	private static final String[][] elementPath = {{"root"}, {"rssmain", "rssacte", "rssda", "rssdad"}};

	private static final String[][] propertyPath = {{"root"}, {"rssheader", "rssmain", "rssacte", "rssda", "rssdad"}, {"*"}};

}
