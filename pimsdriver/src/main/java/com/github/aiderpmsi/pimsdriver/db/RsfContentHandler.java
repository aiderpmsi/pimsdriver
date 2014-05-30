package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RsfContentHandler extends ContentHandlerHelper {

	private static final int LINE_NUMBER = 0;
	private static final int ELEMENT = 1;
	private static final int PROPERTY = 2;
	private static final int ELSE = 3;

	/** Stores if we are in a line number, element, property on somewhere else */
	private int position = ELSE;
	
	/** Colligates the elements of charachter for each element */
	private StringBuilder[] currentPropertyContent = new StringBuilder[4];
	
	/** Name of the property */
	private String currentProperty;
	
	/** NumLine */
	private String lineNumber = "0";

	/** Defines properties for the current element. */
	private List<String> propertieskeys;
	private List<String> propertiesvalues;
	
	/** Upload PK in DB (plud_id) */
	private Long uploadPKId;
	
	/** Process that makes the link with the db */
	private DbLink dblink;
	
	/** Queue between main process and child process */
	private LinkedBlockingQueue<RsfChEntry> queue = new LinkedBlockingQueue<>(1000);
	
	/** future of dblink */
	private Future<Boolean> future = null;
	
	public RsfContentHandler(Connection con, Long uploadPKId) throws SQLException {
		this.uploadPKId = uploadPKId;
		this.dblink = new DbLink(con, queue);
	}

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
			RsfChEntry entry = new RsfChEntry();
			entry.finished = true;
			queue.add(entry);
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
		
		if (isElement(numLinePath)) {
			// IF THIS ELEMENT IS A NEW LINE, REINIT THE CORRESPONDING CURRENTPROPERTYCONTENT
			currentPropertyContent[LINE_NUMBER] = new StringBuilder();
			position = LINE_NUMBER;
		} else if (isElement(elementPath)) {
			// IF THIS ELEMENT IS A NEW ELEMENT, REINIT THE PROPERTIES
			propertieskeys = new LinkedList<>();
			propertiesvalues = new LinkedList<>();
			propertieskeys.add("linenumber");
			propertiesvalues.add(lineNumber);
			position = ELEMENT;
		} else if (isElement(propertyPath)) {
			// IF WE ARE IN A PROPERTY ELEMENT, GET THE NAME OF THIS ELEMENT AND REINIT THE CONTENT
			currentProperty = localName;
			currentPropertyContent[PROPERTY] = new StringBuilder();
			position = PROPERTY;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// IF WE ARE LEAVING AN PROPERTY
		if (position == PROPERTY) {
			propertieskeys.add(currentProperty);
			propertiesvalues.add(currentPropertyContent[PROPERTY].toString());
			position = ELEMENT;
		}
		// IF WE ARE LEAVING AN ELEMENT, SEND IT TO THE PROCESS STORING IN DB
		else if (position == ELEMENT) {
			RsfChEntry entry = new RsfChEntry();
			entry.pmel_root = uploadPKId;
			entry.pmel_type = getContentPath().getLast();
			entry.attributeskeys = propertieskeys;
			entry.attributesvalues = propertiesvalues;
			queue.add(entry);
			position = ELSE;
		}
		// IF WE ARE LEAVING A NUMLINE, SAVE IT AND GO BACK
		else if (position == LINE_NUMBER) {
			lineNumber = currentPropertyContent[LINE_NUMBER].toString();
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
			currentPropertyContent[position].append(ch, start, length);
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

	public class RsfChEntry {
		public boolean finished = false;
		public Long pmel_root;
		public String pmel_type;
		public List<String> attributeskeys;
		public List<String> attributesvalues;
	}
	
	public class DbLink implements Callable<Boolean> {

		/** Header PK in DB */
		private Long headerPKId = null;

		/** Database link */
		private Connection con;

		/** Prepared statement form connection in constructor */
		private PreparedStatement ps;

		/** Queue serving as an ipc */
		private LinkedBlockingQueue<RsfChEntry> queue = new LinkedBlockingQueue<>(1000);

		public DbLink(Connection con, LinkedBlockingQueue<RsfChEntry> queue) throws SQLException {
			this.con = con;
			this.queue = queue;
			// CREATES QUERY
			ps = con.prepareStatement(query);
		}
		
		@Override
		public Boolean call() throws InterruptedException, SQLException {
			while (true) {
				RsfChEntry entry = queue.poll(1, TimeUnit.SECONDS);

				if (entry == null) {
					continue;
				} else if (entry.finished) {
					break;
				} else {
					// CREATES THE ARRAY OF ARGUMENTS (KEYS AND VALUES) FOUND IN RSF
					Array argskeysarray = con.createArrayOf("text", entry.attributeskeys.toArray());
					Array argsvaluesarray = con.createArrayOf("text", entry.attributesvalues.toArray());
					
					// SETS THE VALUES OF QUERY ARGS
					ps.setLong(1, entry.pmel_root);
					if (headerPKId == null)
						ps.setNull(2, Types.BIGINT);
					else
						ps.setLong(2, headerPKId);
					ps.setString(3, entry.pmel_type);
					ps.setArray(4, argskeysarray);
					ps.setArray(5, argsvaluesarray);
					
					ResultSet rs = ps.executeQuery();
					
					// IF THIS ELEMENT IS THE HEADER, USE THIS ELEMENT ID AS PARENT ID (HEADER ID)
					if (getContentPath().getLast().equals("rsfheader")) {
						rs.next();
						headerPKId = rs.getLong(1);
					}
				}
			}
			return null;
		}
		
	}
	
	private boolean isElement(String[][] eltDef) {
		List<String> contentPath = getContentPath();
		
		// IF WE HAVE NOT THE CORRECT NUMBER OF ARGUMENTS, GO AWAY
		if (contentPath.size() != eltDef.length)
			return false;
		
		// CHECK THE ELEMENTS ARE ACCEPTED
		
		Iterator<String> it = contentPath.iterator();
		int i = 0;
		while (it.hasNext()) {
			boolean found = false;
			String element = it.next();
			
			for (int j = 0 ; j < eltDef[i].length ; j++) {
				if (eltDef[i].equals("*") || element.equals(eltDef[i][j])) {
					found = true;
					break;
				}
			}
			
			if (found == false)
				return false;
			
			i++;
		}
		
		// ALL ELEMENTS ARE GOOD
		return true;
	}
	
	private static final String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_attributes) "
			+ "VALUES(?, ?, ?, hstore(?::text[], ?::text[])) RETURNING pmel_id";

	private static final String[][] numLinePath = {{"root"}, {"numline"}};

	private static final String[][] elementPath = {{"root"}, {"rsfheader", "rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}};

	private static final String[][] propertyPath = {{"root"}, {"rsfheader", "rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}, {"*"}};
	
}
