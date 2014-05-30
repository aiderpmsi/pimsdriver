package com.github.aiderpmsi.pimsdriver.db;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class PmsiContentHandlerHelper extends ContentHandlerHelper {

	private static final int LINE_NUMBER = 0;
	private static final int HEADER = 1;
	private static final int ELEMENT = 2;
	private static final int PROPERTY = 3;
	private static final int PROPERTY_HEADER = 4;
	private static final int ELSE = 5;

	/** Stores if we are in a line number, element, property on somewhere else */
	private int position = ELSE;
	
	/** Stores the elements of charachter for each element */
	private StringBuilder content;
	
	/** NumLine */
	private String lineNumber = "0";
	
	/** Finess */
	private StringBuilder finess = new StringBuilder();
	
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

		// UPDATES POSITION
		position =
				isElement(getNumLinePath()) ? LINE_NUMBER :
				(isElement(getElementPath()) ? ELEMENT :
				(isElement(getHeaderPath()) ? HEADER :
				(isElement(getPropertyPath()) ?	(position == ELEMENT ? PROPERTY : PROPERTY_HEADER) : ELSE)));
		
		if (position == LINE_NUMBER || position == ELEMENT || position == HEADER) {
			// REINIT THE CONTENT OF THIS LINE
			content = new StringBuilder();
		}
		
		if (position == HEADER) {
			version = atts.getValue("version");
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN ELEMENT OR THE HEADER, SEND IT TO THE PROCESSING
		if (position == ELEMENT || position == HEADER) {
			Entry entry = new Entry();
			entry.pmel_type = contentPath.getLast();
			entry.pmel_content = content.toString();
			entry.pmel_line = lineNumber;
			dblink.store(entry);
		}
		
		if (position == PROPERTY) {
			// IF WE ARE LEAVING A PROPERTY
			position = ELEMENT;
		} else if (position == PROPERTY_HEADER) {
			position = HEADER;
		} else if (position == LINE_NUMBER) {
			lineNumber = content.toString();
			position = ELSE;
		} else if (position == ELEMENT || position == HEADER) {
			position = ELSE;
		}

		// BE SURE TO DECREMENT DEPTH
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE IN PROPERTY OR NUMLINE
		if (position == PROPERTY || position == PROPERTY_HEADER || position == LINE_NUMBER) {
			content.append(ch, start, length);
		}
		
		if (position == PROPERTY_HEADER && contentPath.getLast().equals("Finess")) {
			finess.append(ch, start, length);
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
	
	public String getVersion() {
		return version;
	}

	public String getFiness() {
		return finess.toString();
	}
	
	public void close() throws SAXException {
		try {
			if (future != null) {
				future.cancel(true);
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new SAXException(e);
		} finally {
			future = null;
		}
	}

	public abstract String[][] getNumLinePath();
	
	public abstract String[][] getElementPath();

	public abstract String[][] getHeaderPath();

	public abstract String[][] getPropertyPath();
	
}
