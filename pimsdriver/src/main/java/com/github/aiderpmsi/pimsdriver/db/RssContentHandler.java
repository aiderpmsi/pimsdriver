package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RssContentHandler extends PmsiContentHandlerHelper {

	public RssContentHandler(Connection con, Long uploadPKId) throws SQLException {
		dblink = new RssDbLink(con, uploadPKId);
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

	private static final String[][] numLinePath = {{"root"}, {"numline"}};

	private static final String[][] elementPath = {{"root"}, {"rssheader", "rssmain", "rssacte", "rssda", "rssdad"}};

	private static final String[][] propertyPath = {{"root"}, {"rssheader", "rssmain", "rssacte", "rssda", "rssdad"}, {"*"}};

}
