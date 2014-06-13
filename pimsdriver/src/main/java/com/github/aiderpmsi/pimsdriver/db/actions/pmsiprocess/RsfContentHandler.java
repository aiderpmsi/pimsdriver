package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.sql.Connection;
import java.sql.SQLException;

public class RsfContentHandler extends PmsiContentHandlerHelper {

	public RsfContentHandler(Connection con, Long uploadPKId, Long pmsiPosition) throws SQLException {
		dblink = new RsfDbLink(con, uploadPKId, pmsiPosition);
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

	private static final String[][] headerPath = {{"root"}, {"rsfheader"}};

	private static final String[][] elementPath = {{"root"}, {"rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}};

	private static final String[][] propertyPath = {{"root"}, {"rsfheader", "rsfa", "rsfb", "rsfc", "rsfh", "rsfi", "rsfl", "rsfm"}, {"*"}};


}
