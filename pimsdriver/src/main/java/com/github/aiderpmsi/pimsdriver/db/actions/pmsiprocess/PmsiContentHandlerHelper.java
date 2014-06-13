package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class PmsiContentHandlerHelper extends ContentHandlerHelper implements AutoCloseable {

	protected enum State {
		LINE_NUMBER, HEADER, ELEMENT, PROPERTY, PROPERTY_HEADER, ELSE;
	}

	/** Stores if we are in a line number, element, property on somewhere else */
	protected State position = State.ELSE;
	
	/** Stores the elements of charachter for each element */
	protected StringBuilder content;
	
	/** NumLine */
	protected String lineNumber = "0";
	
	/** Finess */
	protected StringBuilder finess = new StringBuilder();
	
	/** Version of the rsf */
	protected String version;

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
		// DO NOTHING
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
				isElement(getNumLinePath()) ? State.LINE_NUMBER :
				(isElement(getElementPath()) ? State.ELEMENT :
				(isElement(getHeaderPath()) ? State.HEADER :
				(isElement(getPropertyPath()) ?	(position == State.ELEMENT ? State.PROPERTY : State.PROPERTY_HEADER) : State.ELSE)));
		
		if (position == State.LINE_NUMBER || position == State.ELEMENT || position == State.HEADER) {
			// REINIT THE CONTENT OF THIS LINE
			content = new StringBuilder();
		}
		
		if (position == State.HEADER) {
			version = atts.getValue("version");
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN ELEMENT OR THE HEADER, SEND IT TO THE PROCESSING
		if (position == State.ELEMENT || position == State.HEADER) {
			Entry entry = new Entry();
			entry.pmel_type = contentPath.getLast();
			entry.pmel_content = content.toString();
			entry.pmel_line = lineNumber;
			try {
				dblink.store(entry);
			} catch (InterruptedException e) {
				throw new SAXException(e);
			}
		}
		
		if (position == State.PROPERTY) {
			// IF WE ARE LEAVING A PROPERTY
			position = State.ELEMENT;
		} else if (position == State.PROPERTY_HEADER) {
			position = State.HEADER;
		} else if (position == State.LINE_NUMBER) {
			lineNumber = content.toString();
			position = State.ELSE;
		} else if (position == State.ELEMENT || position == State.HEADER) {
			position = State.ELSE;
		}

		// BE SURE TO DECREMENT DEPTH
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE IN PROPERTY OR NUMLINE
		if (position == State.PROPERTY || position == State.PROPERTY_HEADER || position == State.LINE_NUMBER) {
			content.append(ch, start, length);
		}
		
		if (position == State.PROPERTY_HEADER && contentPath.getLast().equals("Finess")) {
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
	
	public long getPmsiPosition() {
		return dblink.pmsiPosition;
	}

	@Override
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
