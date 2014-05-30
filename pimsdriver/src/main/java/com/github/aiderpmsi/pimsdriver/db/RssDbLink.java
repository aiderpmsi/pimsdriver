package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RssDbLink extends DbLink {

	private boolean wasHeader = false;
	
	private boolean wasMain = false;

	long rssHeader;
	
	long rssMain;
	
	private Long currentParent = null;
	
	public RssDbLink(Connection con, long pmel_root) throws SQLException {
		super(con, pmel_root);
	}
	@Override
	protected Long getParent() {
		return currentParent;
	}

	@Override
	protected void calculateParent(Entry entry) {
		// IF LAST ELEMENT WAS RSSHEADER, STORE IT
		if (wasHeader)
			rssHeader = last_line;
		else if (wasMain)
			rssMain = last_line;
		
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

}
