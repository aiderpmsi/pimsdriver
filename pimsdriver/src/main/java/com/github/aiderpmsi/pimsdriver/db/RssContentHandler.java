package com.github.aiderpmsi.pimsdriver.db;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.copy.CopyManager;
import org.xml.sax.SAXException;

public class RssContentHandler extends PmsiContentHandlerHelper {

	protected StringBuilder propertyContent = new StringBuilder();
	
	protected HashMap<String, String> propertyValues = new HashMap<>();
	
	protected GroupDbLink groupdblink;
	
	protected Future<Path> groupFuture;
	
	/** Copy Manager */
	private CopyManager cm;
		
	public RssContentHandler(Connection con, Long uploadPKId, long pmsiPosition) throws IOException, SQLException {
		@SuppressWarnings("unchecked")
		Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
		cm = new CopyManager((org.postgresql.core.BaseConnection)conn);
		groupdblink = new GroupDbLink(pmsiPosition);
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
		Path tmpFile = null;
		try {
			GroupEntry groupEntry = new GroupEntry();
			groupEntry.finished = true;
			groupdblink.store(groupEntry);
			tmpFile = groupFuture.get();
		} catch (InterruptedException e) {
			// DO NOTHING, WE HAVE TO END
		} catch (ExecutionException e) {
			throw new SAXException(e);
		} finally {
			groupFuture = null;
			super.endDocument();
			// SUBPROCESSES HAVE FINISHED, CURRENT PROCESS CAN NOW COPY THE TMP FILE WITH GROUPS IN DATABASE
			Reader reader = null;
			try {
				reader = Files.newBufferedReader(tmpFile, Charset.forName("UTF-8"));
				cm.copyIn(query, reader);
			} catch (IOException | SQLException e) {
				throw new SAXException(e);
			} finally {
				if (reader != null) try {reader.close();} catch (IOException e) {throw new SAXException(e);}
				try {Files.deleteIfExists(tmpFile);} catch (IOException e) {throw new SAXException(e);}
			}
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

	private static final String query = "COPY pmgr_temp (pmel_position, pmgr_racine, pmgr_modalite, pmgr_gravite, pmgr_erreur) "
			+ "FROM STDIN WITH DELIMITER '|'";
}
