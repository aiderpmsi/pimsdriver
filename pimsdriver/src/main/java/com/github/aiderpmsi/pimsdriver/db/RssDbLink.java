package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RssDbLink extends DbLink {

	private boolean wasHeader = false;
	
	private boolean wasMain = false;

	long rssHeader;
	
	long rssMain;
	
	private Long currentParent = null;
	
	public RssDbLink(Connection con, long pmel_root, long pmsiPosition) throws SQLException {
		super(con, pmel_root, pmsiPosition);
	}
	@Override
	protected Long getParent() {
		return currentParent;
	}

	@Override
	protected void calculateParent(Entry entry) {
		// IF LAST ELEMENT WAS RSSHEADER, STORE IT
		if (wasHeader)
			rssHeader = pmsiPosition - 1;
		else if (wasMain)
			rssMain = pmsiPosition - 1;
		
		// CHECKS IF THIS ELEMENT IS HEADER OR MAIN FOR NEXT ITERATION THROUGH CALCULATEPARENT
		if (entry.pmel_type.equals("rssheader")) {
			wasHeader = true;
			wasMain = false;
			currentParent = null;
		} else if (entry.pmel_type.equals("rssmain")){
			wasHeader = false;
			wasMain = true;
			currentParent = rssHeader;
		} else {
			wasHeader = false;
			wasMain = false;
			currentParent = rssMain;
		}

	}

	@Override
	protected CharSequence getRootType() {
		return rootType;
	}

	private static final String rootType = "rss";

}
