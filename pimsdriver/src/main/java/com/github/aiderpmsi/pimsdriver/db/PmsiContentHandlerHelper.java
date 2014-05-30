package com.github.aiderpmsi.pimsdriver.db;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class PmsiContentHandlerHelper extends ContentHandlerHelper {

	private static final int LINE_NUMBER = 0;
	private static final int ELEMENT = 1;
	private static final int PROPERTY = 2;
	private static final int ELSE = 3;

	/** Stores if we are in a line number, element, property on somewhere else */
	private int position = ELSE;
	
	/** Stores the elements of charachter for each element */
	private StringBuilder content;
	
	/** NumLine */
	private String lineNumber = "0";
	
	/** Version of the rsf */
	private String version;

	/** Process that makes the link with the db */
	protected DbLink dblink;
	
	/** future of dblink */
	private Future<Boolean> future = null;
	
	//======== METHODS FOR CONTENTHANDLER =======
	@Override
	public void setDocumentLocator(Locator locator) {
		// DO NOTHING
	}

	@Override
	public void startDocument() throws SAXException {
		// WHEN WE ENTER IN THIS DOCUMENT, START THE DB LINK
		future = Executors.newSingleThreadExecutor().submit(dblink);
	}

	@Override
	public void endDocument() throws SAXException {
		// WE HAVE TO WAIT THE DB LINK TO END
		try {
			Entry entry = new Entry();
			entry.finished = true;
			dblink.store(entry);
			future.get();
		} catch (InterruptedException e) {
			// DO NOTHING, WE HAVE TO END
		} catch (ExecutionException e) {
			throw new SAXException(e);
		} finally {
			future = null;
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// DO NOTHIN
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// DO NOTHING
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		// COUNT THE ARRIVAL IN THIS NEW ELEMENT
		super.startElement(uri, localName, qName, atts);
		
		if (isElement(getNumLinePath())) {
			// IF THIS ELEMENT IS A NEW LINE, REINIT THE CONTENT
			content = new StringBuilder();
			position = LINE_NUMBER;
		} else if (isElement(getElementPath())) {
			// IF THIS ELEMENT IS A NEW ELEMENT, REINIT THE PROPERTIES AND ADDS LINE NUMBER PROPERTY
			content = new StringBuilder();
			// IF THIS ELEMENT IS RSFHEADER, GETS THE VERSION FROM ATTRIBUTES
			if (contentPath.getLast().equals("rsfheader"))
				version = atts.getValue("version");
			position = ELEMENT;
		} else if (isElement(getPropertyPath())) {
			// IF WE ARE IN A PROPERTY ELEMENT, DO NOTHING
			position = PROPERTY;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN PROPERTY
		if (position == PROPERTY) {
			position = ELEMENT;
		}
		// IF WE ARE LEAVING AN ELEMENT, SEND IT TO THE PROCESS STORING IN DB
		else if (position == ELEMENT) {
			Entry entry = new Entry();
			entry.pmel_type = contentPath.getLast();
			entry.pmel_content = content.toString();
			entry.pmel_line = lineNumber;
			dblink.store(entry);
			position = ELSE;
		}
		// IF WE ARE LEAVING A NUMLINE, SAVE IT AND GO BACK
		else if (position == LINE_NUMBER) {
			lineNumber = content.toString();
			position = ELSE;
		}
		
		// BE SURE TO DECREMENT DEPTH
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE IN PROPERTY OR NUMLINE
		if (position == PROPERTY || position == LINE_NUMBER) {
			content.append(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// Do nothing
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// Do nothing
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// Do nothing
	}
	
	public void close() {
		try {
			if (future != null) {
				future.cancel(true);
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			// DO NOTHING, WE HAVE TO END
		} finally {
			future = null;
		}
	}

	public String getVersion() {
		return version;
	}

	public abstract String[][] getNumLinePath();
	
	public abstract String[][] getElementPath();

	public abstract String[][] getPropertyPath();
	
}
