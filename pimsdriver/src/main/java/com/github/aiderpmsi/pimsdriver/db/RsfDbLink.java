package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RsfDbLink extends DbLink {

	private boolean wasHeader = false;
	
	private boolean wasRsfA = false;

	private long rsfHeader;
	
	private long rsfA;
	
	private Long currentParent = null;
	
	public RsfDbLink(Connection con, long pmel_root, long pmsiPosition) throws SQLException {
		super(con, pmel_root, pmsiPosition);
	}
	@Override
	protected Long getParent() {
		return currentParent;
	}

	@Override
	protected void calculateParent(Entry entry) {
		// IF LAST ELEMENT WAS RSFHEADER, USE ITS ID AS THE PARENT ID FOR EVERY ELEMENT
		if (wasHeader)
			rsfHeader = pmsiPosition - 1;
		else if (wasRsfA)
			rsfA = pmsiPosition - 1;
			
		// CHECKS IF THIS ELEMENT IS HEADER OR RSFA FOR NEXT ITERATION THROUGH CALCULATEPARENT
		if (entry.pmel_type.equals("rsfheader")) {
			wasHeader = true;
			wasRsfA = false;
			currentParent = null;
		} else if (entry.pmel_type.equals("rsfa")) {
			wasHeader = false;
			wasRsfA = true;
			currentParent = rsfHeader;
		} else {
			wasHeader = false;
			wasRsfA = false;
			currentParent = rsfA;
		}

	}
	
	@Override
	protected CharSequence getRootType() {
		return rootType;
	}

	private static final String rootType = "rsf";
}
