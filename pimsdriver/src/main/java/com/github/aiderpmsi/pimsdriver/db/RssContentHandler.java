package com.github.aiderpmsi.pimsdriver.db;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.xml.sax.SAXException;

public class RssContentHandler extends PmsiContentHandlerHelper {

	protected StringBuilder propertyContent = new StringBuilder();
	
	protected HashMap<String, String> propertyValues = new HashMap<>();
	
	protected GroupDbLink groupdblink;
	
	protected Future<Path> groupFuture;
		
	public RssContentHandler(Connection con, Long uploadPKId, long pmsiPosition) throws IOException, SQLException {
		groupdblink = new GroupDbLink();
		dblink = new RssDbLink(con, uploadPKId, pmsiPosition);
	}
	
	@Override
	public void startDocument() throws SAXException {
		// WHEN WE ENTER IN THIS DOCUMENT, START THE DB LINK
		groupFuture = Executors.newSingleThreadExecutor().submit(groupdblink);
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		// WE HAVE TO WAIT THE GROUP LINK TO END
		try {
			GroupEntry groupEntry = new GroupEntry();
			groupEntry.finished = true;
			groupdblink.store(groupEntry);
			groupFuture.get();
		} catch (InterruptedException e) {
			// DO NOTHING, WE HAVE TO END
		} catch (ExecutionException e) {
			throw new SAXException(e);
		} finally {
			groupFuture = null;
			super.endDocument();
		}
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
