package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RssContentHandler extends ContentHandlerHelper {

	/** Regexp to know if we are in an element */
	private static Pattern inElement = Pattern.compile("/root/(?:rssheader|rssmain|rssacte|rssda|rssdad)");
	
	/** Regexp to know if we are in a property */
	private static Pattern inProperty = Pattern.compile("/root/(?:rssheader|rssmain|rssacte|rssda|rssdad)/.+");
	
	/** Colligates the elements of charachter */
	private StringBuilder currentPropertyContent;
	
	/** Name of the property */
	private String currentProperty;

	/** Defines properties for the current element. */
	private List<String> propertieskeys;
	private List<String> propertiesvalues;
	
	/** Upload PKID in Database */
	private Long uploadPKId;
	
	/** Process that makes the link with the db */
	private DbLink dblink;
	
	/** Queue between main process and child process */
	private LinkedBlockingQueue<RssChEntry> queue = new LinkedBlockingQueue<>(1000);
	
	/** future of dblink */
	private Future<Boolean> future = null;
	
	public RssContentHandler(Connection con, Long uploadPKId) throws SQLException {
		this.uploadPKId = uploadPKId;
		dblink = new DbLink(con, queue);
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
			RssChEntry entry = new RssChEntry();
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
			RssChEntry entry = new RssChEntry();
			entry.pmel_root = uploadPKId;
			entry.pmel_type = getContentPath().getLast();
			entry.attributeskeys = propertieskeys;
			entry.attributesvalues = propertiesvalues;
			queue.add(entry);
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
	public class RssChEntry {
		public boolean finished = false;
		public Long pmel_root;
		public String pmel_type;
		public List<String> attributeskeys;
		public List<String> attributesvalues;
	}

	public class DbLink implements Callable<Boolean> {

		/** Header PKID in Database */
		private Long headerPKId = null;
		
		/** Main PKID in Database */
		private Long mainPKId = null;
		
		/** Database link */
		private Connection con;
		
		/** Query to insert datas in rss table */
		private PreparedStatement ps;

		/** Queue serving as an ipc */
		private LinkedBlockingQueue<RssChEntry> queue = new LinkedBlockingQueue<>(1000);

		public DbLink(Connection con, LinkedBlockingQueue<RssChEntry> queue) throws SQLException {
			this.con = con;
			this.queue = queue;
			// CREATES QUERY
			ps = con.prepareStatement(query);
		}

		@Override
		public Boolean call() throws InterruptedException, SQLException {
			while (true) {
				RssChEntry entry = queue.poll(1, TimeUnit.SECONDS);

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
					if (entry.pmel_type.equals("rssheader"))
						ps.setNull(2, Types.BIGINT);
					else if (entry.pmel_type.equals("rssmain"))
						ps.setLong(2, headerPKId);
					else
						ps.setLong(2, mainPKId);
					ps.setString(3, entry.pmel_type);
					ps.setArray(4, argskeysarray);
					ps.setArray(5, argsvaluesarray);

					ResultSet rs = ps.executeQuery();
					
					// IF THIS ELEMENT IS THE HEADER, STORE THE INSERTED ID AS HEADERID
					if (entry.pmel_type.equals("rssheader")) {
						rs.next();
						headerPKId = rs.getLong(1);
					}
					// IF THIS ELEMENT IS A MAIN ELEMENT, STORE THE INSERTED ID AS MAINID
					else if (entry.pmel_type.equals("rssmain")) {
						rs.next();
						mainPKId = rs.getLong(1);
					}
				}
			}
			return null;
		}

	}

	private static final String query = "INSERT INTO pmel_temp (pmel_root, pmel_parent, pmel_type, pmel_attributes) "
			+ "VALUES(?, ?, ?, hstore(?::text[], ?::text[])) RETURNING pmel_id";

}
