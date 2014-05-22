package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RssContentHandler extends ContentHandlerHelper {

	/** Regexp to know if we are in an element */
	private Pattern inElement = Pattern.compile("/root/(?:rssheader|rssmain|rssacte|rssda|rssdad)");
	
	/** Regexp to know if we are in a property */
	private Pattern inProperty = Pattern.compile("/root/(?:rssheader|rssmain|rssacte|rssda|rssdad)/.+");
	
	/** Colligates the elements of charachter */
	private StringBuilder currentPropertyContent;
	
	/** Name of the property */
	private String currentProperty;

	/** Defines properties for the current element. */
	private List<String> propertieskeys;
	private List<String> propertiesvalues;
	
	/** Upload PKID in Database */
	private Long uploadPKId;
	
	/** Header PKID in Database */
	private Long headerPKId = null;
	
	/** Main PKID in Database */
	private Long mainPKId = null;
	
	/** Database link */
	private Connection con;
	
	/** Query to insert datas in rss table */
	private PreparedStatement ps;

	public RssContentHandler(Connection con, Long uploadPKId) throws SQLException {
		this.con = con;
		this.uploadPKId = uploadPKId;
		ps = con.prepareStatement(query);
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
		
		// FINDS IF THIS ELEMENT IS A NEW ELEMENT AND MUST REINIT THE PROPERTIES AND MAINID
		if (getContentPath().size() == 2 && inElement.matcher(getPath()).matches()) {
			propertieskeys = new LinkedList<>();
			propertiesvalues = new LinkedList<>();
			// WE ENTER IN A NEW RSSMAIN, WE HAVE TO SET THE MAIN ID TO NULL
			if (getContentPath().getLast().equals("rssmain"))
				mainPKId = null;
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
			propertieskeys.add(currentProperty);
			propertiesvalues.add(currentPropertyContent.toString());
		}
		// IF WE ARE LEAVING AN ELEMENT, STORE IT IN DB
		else if (getContentPath().size() == 2 && inElement.matcher(getPath()).matches()) {
			// GENERATES THE QUERY

			try {
				Array argskeysarray = con.createArrayOf("text", propertieskeys.toArray());
				Array argsvaluesarray = con.createArrayOf("text", propertiesvalues.toArray());
			
				// SETS THE VALUES OF QUERY ARGS
				ps.setLong(1, uploadPKId);
				Long parentId = (mainPKId == null ? headerPKId : mainPKId);
				if (parentId == null)
					ps.setNull(2, Types.BIGINT);
				else
					ps.setLong(2, parentId);
				ps.setString(3, getContentPath().getLast());
				ps.setArray(4, argskeysarray);
				ps.setArray(5, argsvaluesarray);

				ResultSet rs = ps.executeQuery();
				
				// IF THIS ELEMENT IS THE HEADER, STORE THE INSERTED ID AS HEADERID
				if (getContentPath().getLast().equals("rssheader")) {
					rs.next();
					headerPKId = rs.getLong(1);
				}
				// IF THIS ELEMENT IS A MAIN ELEMENT, STORE THE INSERTED ID AS MAINID
				else if (getContentPath().getLast().equals("rssmain")) {
					rs.next();
					mainPKId = rs.getLong(1);
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

	private static final String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_attributes) "
			+ "VALUES(?, ?, ?, hstore(?::text[], ?::text[])) RETURNING pmel_id";

}
