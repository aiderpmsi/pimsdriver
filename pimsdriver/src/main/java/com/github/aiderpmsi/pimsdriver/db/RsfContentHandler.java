package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RsfContentHandler extends PmsiContentHandlerHelper {

	public RsfContentHandler(Connection con, Long uploadPKId) throws SQLException {
		dblink = new RsfDbLink(con, uploadPKId);
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

	private static final String[][] elementPath = {{"root"}, {"rsfheader", "rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}};

	private static final String[][] propertyPath = {{"root"}, {"rsfheader", "rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}, {"*"}};

}
