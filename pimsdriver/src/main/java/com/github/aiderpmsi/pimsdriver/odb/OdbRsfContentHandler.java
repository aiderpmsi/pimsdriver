package com.github.aiderpmsi.pimsdriver.odb;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class OdbRsfContentHandler extends ContentHandlerHelper {

	/**
	 * Regexp to know if we are in an element
	 */
	private Pattern inElement = Pattern.compile("/root/(?:rsfheader|rsfa|rsfb|rsfc|rsfh|rsfi|rsfl|rsfm)");
	
	/**
	 * Regexp to know if we are in a property
	 */
	private Pattern inProperty = Pattern.compile("/root/(?:rsfheader|rsfa|rsfb|rsfc|rsfh|rsfi|rsfl|rsfm)/.+");
	
	/**
	 * Colligates the elements of charachter
	 */
	private StringBuilder currentPropertyContent;
	
	/**
	 * Name of the property
	 */
	private String currentProperty;

	/**
	 * Defines properties for the current element.
	 */
	private HashMap<String, String> properties;
	
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
		// COUNT THE ARRIVAL IN THIS NEW ELEMENT
		super.startElement(uri, localName, qName, atts);
		
		// FINDS IF THIS ELEMENT IS A NEW ELEMENT AND MUST REINIT THE PROPERTIES
		if (getContentPath().size() == 2 && inElement.matcher(getPath()).matches()) {
			properties = new HashMap<String, String>();
		}
		// IF WE ARE AT DEPTH 2 AND IN AN ELEMENT, GET THE PROPERTY NAME
		else if (getContentPath().size() == 3 && inProperty.matcher(getPath()).matches()) {
			currentProperty = localName;
			currentPropertyContent = new StringBuilder();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN PROPERTY
		if (getContentPath().size() == 3 && inProperty.matcher(getPath()).matches()) {
			properties.put(currentProperty, currentPropertyContent.toString());
		}
		// IF WE ARE LEAVING AN ELEMENT, STORE IT IN DB
		else if (getContentPath().size() == 2 && inElement.matcher(getPath()).matches()) {
			// CREATES THE ENTRY IN THE RIGHT CLASS
			ODocument odoc = tx.newInstance("PmsiElement");
			odoc.field("type", getContentPath().getLast());

			// STORES THE PROPERTIES
			for (Entry<String, String> property : properties.entrySet()) {
				odoc.field(property.getKey(), property.getValue());
			}
			
			// IF THIS ELEMENT IS THE HEADER, STORE THE PARENTLINK AS UPLOAD ELEMENT
			// AND SETS THIS ELEMENT AS HEADERID
			if (getContentPath().getLast().equals("rsfheader")) {
				odoc.field("parentlink", uploadID, OType.LINK);
				tx.save(odoc);
				headerID = odoc.getIdentity();
			}
			// IF THIS ELEMENT IS NOT THE HEADER, STORE THE HEADERID AS THE PARENT
			else {
				odoc.field("parentlink", headerID, OType.LINK);
				tx.save(odoc);
			}
		}
		
		// BE SURE TO DECREMENT DEPTH
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// IF WE ARE NOT OUT AND AT DEPTH 3, APPEND THOSE CHARACTERS TO THE CONTENT OF CURRENT PROPERTY
		if (getContentPath().size() == 3 && inProperty.matcher(getPath()).matches()) {
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
