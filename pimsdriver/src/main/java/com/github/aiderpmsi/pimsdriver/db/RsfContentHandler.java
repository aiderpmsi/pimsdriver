package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RsfContentHandler extends ContentHandlerHelper {

	/** Regexp to know if we are in an element */
	private Pattern inElement = Pattern.compile("/root/(?:rsfheader|rsfa|rsfb|rsfc|rsfh|rsfi|rsfl|rsfm)");
	
	/** Regexp to know if we are in a property */
	private Pattern inProperty = Pattern.compile("/root/(?:rsfheader|rsfa|rsfb|rsfc|rsfh|rsfi|rsfl|rsfm)/.+");
	
	/** Colligates the elements of charachter */
	private StringBuilder currentPropertyContent;
	
	/** Name of the property */
	private String currentProperty;

	/** Defines properties for the current element. */
	private HashMap<String, String> properties;
	
	/** Upload PK in DB (plud_id) */
	private Long uploadPKId;
	
	/** Header PK in DB */
	private Long headerPKId = null;
	
	/** Database link */
	private Connection con;

	public RsfContentHandler(Connection con, Long uploadPKId) {
		this.con = con;
		this.uploadPKId = uploadPKId;
	}

	//======== METHODS FOR CONTENTHANDLER =======
	@Override
	public void setDocumentLocator(Locator locator) {
		// DO NOTHING
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
			// GENERATES THE QUERY
			String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_attributes) "
					+ "VALUES(?, ?, ?, hstore(?::text[], ?::text[])) RETURNING pmel_id";
			try {
				PreparedStatement ps = con.prepareStatement(query);
				
				// CREATES THE ARRAY OF ARGUMENTS (KEYS AND VALUES) FOUND IN RSF
				List<String> argskeys = new ArrayList<>(properties.size());
				List<String> argsvalues = new ArrayList<>(properties.size());
				for (Entry<String, String> property : properties.entrySet()) {
					argskeys.add(property.getKey());
					argsvalues.add(property.getValue());
				}
	
				Array argskeysarray = con.createArrayOf("text", argskeys.toArray(new String[properties.size()]));
				Array argsvaluesarray = con.createArrayOf("text", argsvalues.toArray(new String[properties.size()]));
				
				// SETS THE VALUES OF QUERY ARGS
				ps.setLong(1, uploadPKId);
				if (headerPKId == null)
					ps.setNull(2, Types.BIGINT);
				else
					ps.setLong(2, headerPKId);
				ps.setString(3, getContentPath().getLast());
				ps.setArray(4, argskeysarray);
				ps.setArray(5, argsvaluesarray);
				
				ResultSet rs = ps.executeQuery();
				
				// IF THIS ELEMENT IS THE HEADER, USE THIS ELEMENT ID AS PARENT ID (HEADER ID)
				if (getContentPath().getLast().equals("rsfheader")) {
					rs.next();
					headerPKId = rs.getLong(1);
				}
			} catch (SQLException e) {
				throw new SAXException(e);
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
