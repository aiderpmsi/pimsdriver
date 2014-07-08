package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.IOException;

import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine.Element;

public class RsfLineHandler extends PmsiLineHandler {

	private boolean wasHeader = false;
	
	private boolean wasRsfA = false;

	private long rsfHeader;
	
	private long rsfA;
	
	private Long currentParent = null;
	
	private String finess = null;
	
	private String version = null;
	
	public RsfLineHandler(final long pmel_root, final long pmsiPosition) throws IOException {
		super(pmel_root, pmsiPosition);
	}
	
	@Override
	protected Long getParent() {
		return currentParent;
	}

	@Override
	protected void calculateParent(IPmsiLine line) {
		// IF LAST ELEMENT WAS RSFHEADER, USE ITS ID AS THE PARENT ID FOR EVERY ELEMENT
		if (wasHeader)
			rsfHeader = pmsiPosition - 1;
		else if (wasRsfA)
			rsfA = pmsiPosition - 1;
			
		// CHECKS IF THIS ELEMENT IS HEADER OR RSFA FOR NEXT ITERATION THROUGH CALCULATEPARENT
		if (line.getName().equals("rsfheader")) {
			startsearch : for (Element element : line.getElements()) {
				if (element.getName().equals("Finess")) {
					finess = element.getElement().toString();
					break startsearch;
				}
			}
			version = line.getVersion();
			wasHeader = true;
			wasRsfA = false;
			currentParent = null;
		} else if (line.getName().equals("rsfa")) {
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
	public String getFiness() {
		return finess;
	}

	@Override
	public String getVersion() {
		return version;
	}
	
}
