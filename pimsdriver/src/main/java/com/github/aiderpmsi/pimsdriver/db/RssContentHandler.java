package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RssContentHandler extends PmsiContentHandlerHelper {

	public RssContentHandler(Connection con, Long uploadPKId, long pmsiPosition) throws SQLException {
		dblink = new RssDbLink(con, uploadPKId, pmsiPosition);
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

}
