package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class RsfDbLink extends DbLink {

	private boolean wasHeader = false;
	
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
			currentParent = pmsiPosition - 1;
		
		// CHECKS IF THIS ELEMENT IS HEADER FOR NEXT ITERATION THROUGH CALCULATEPARENT
		if (entry.pmel_type.equals("rsfheader")) {
			wasHeader = true;
		} else {
			wasHeader = false;
		}

	}
	
	@Override
	protected CharSequence getRootType() {
		return rootType;
	}

	private static final String rootType = "rsf";
}
