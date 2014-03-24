package com.github.aiderpmsi.pimsdriver.odb;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class OdbRsfContentHandler implements ContentHandler {

	/**
	 * Define possible states for the contenthandler
	 * @author delabre
	 *
	 */
	private enum States {OUT, HEADER, RSFA, RSFB, RSFC, RSFH, RSFI, RSFL, RSFM};
	
	/**
	 * Define current state for ContentHandler
	 */
	private States state = States.OUT;
	
	/**
	 * Defines properties for the current element.
	 */
	private HashMap<String, String> properties;
	
	/**
	 * Current property name
	 */
	private String currentProperty;
	
	/**
	 * Current property content
	 */
	private StringBuilder currentPropertyContent;
	
	/**
	 * Depth in the xml
	 */
	private int depth = 0;
	
	/**
	 * Upload ORID in Database
	 */
	ORID uploadID;
	
	/**
	 * Header ORID
	 */
	ORID headerID;
	
	/**
	 * Database link
	 */
	private ODatabaseDocumentTx tx;

	public OdbRsfContentHandler(ODatabaseDocumentTx tx, ORID uploadId) throws FileNotFoundException {
		this.tx = tx;
		this.uploadID = uploadId;
	}

	//======== METHODS FOR CONTENTHANDLER =======
	@Override
	public void setDocumentLocator(Locator locator) {
		// Do nothing
	}

	@Override
	public void startDocument() throws SAXException {
		// WE DO NOT HAVE TO DO ANYTHING WHEN WE ENTER A DOCUMENT
	}

	@Override
	public void endDocument() throws SAXException {
		// WE DO NOT HAVE TO DO ANYTHING WHEN WE LEAVE A DOCUMENT
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// Do nothing
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// Do nothing
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {

		// IF WE ARE AT DEPTH 1, FIND IF THIS ELEMENT MAKES US ENTER IN ANOTHER STATE
		if (depth == 1 && state == States.OUT) {
			switch (localName) {
			case "rsfheader": state = States.HEADER; break;
			case "rsfa": state = States.RSFA; break;
			case "rsfb": state = States.RSFB; break;
			case "rsfc": state = States.RSFC; break;
			case "rsfh": state = States.RSFH; break;
			case "rsfi": state = States.RSFI; break;
			case "rsfl": state = States.RSFL; break;
			case "rsfm": state = States.RSFM; break;
			}
			if (state != States.OUT)
				properties = new HashMap<String, String>();
		}
		// IF WE ARE AT DEPTH 2 AND NOT OUT, GET THE ELEMENT NAME
		else if (depth == 2 && state != States.OUT){
			currentProperty = localName;
			currentPropertyContent = new StringBuilder();
		}
		
		// BE SURE TO INCREMENT DEPTH
		depth++;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		// IF WE ARE AT DEPTH 3 AND NOT OUT, STORE THE PROPERTY IN PROPERTIES
		if (depth == 3 && state != States.OUT) {
			properties.put(currentProperty, currentPropertyContent.toString());
		}
		
		// IF WE ARE AT DEPTH 2 SET STATE OUT (WE ARE NOT ANYMORE IN AN INTERESSANT CONTENT)
		// AND STORE PROPERTIES IN DB
		if (depth == 2 && state != States.OUT) {
			// CREATES THE ENTRY IN THE RIGHT CLASS
			ODocument odoc = tx.newInstance("PmsiElement");
			odoc.field("type", state.toString());

			// STORES THE PROPERTIES
			for (Entry<String, String> property : properties.entrySet()) {
				odoc.field(property.getKey(), property.getValue());
			}
			
			// IF THIS ELEMENT IS THE HEADER, STORE THE PARENTLINK AS UPLOAD ELEMENT
			// AND SETS THIS ELEMENT AS HEADERID
			if (state == States.HEADER) {
				odoc.field("parentlink", uploadID);
				tx.save(odoc);
				headerID = odoc.getIdentity();
			}
			// IF THIS ELEMENT IS NOT THE HEADER, STORE THE HEADERID AS THE PARENT
			else {
				odoc.field("parentlink", headerID);
				tx.save(odoc);
			}
			tx.save(odoc);

			// WE ARE NOW OUT
			state = States.OUT;
		}
		
		// BE SURE TO DECREMENT DEPTH
		depth--;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE NOT OUT AND AT DEPTH 3, APPEND THOSE CHARACTERS TO THE CONTENT OF CURRENT PROPERTY
		if (depth == 3 && state != States.OUT) {
			currentPropertyContent.append(ch, start, length);
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
}
