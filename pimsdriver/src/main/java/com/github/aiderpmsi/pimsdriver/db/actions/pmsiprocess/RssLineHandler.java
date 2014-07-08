package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.IOException;

import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine;
import com.github.aiderpmsi.pims.parser.linestypes.IPmsiLine.Element;

public class RssLineHandler extends PmsiLineHandler {

	private boolean wasHeader = false;
	
	private boolean wasMain = false;

	private long rssHeader;
	
	private long rssMain;
	
	private Long currentParent = null;
	
	private String finess = null;
	
	private String version = null;
	
	public RssLineHandler(final long pmel_root, final long pmsiPosition) throws IOException {
		super(pmel_root, pmsiPosition);
	}
	
	@Override
	protected Long getParent() {
		return currentParent;
	}

	@Override
	protected void calculateParent(IPmsiLine line) {
		// IF LAST ELEMENT WAS RSSHEADER, STORE IT
		if (wasHeader)
			rssHeader = pmsiPosition - 1;
		else if (wasMain)
			rssMain = pmsiPosition - 1;
		
		// CHECKS IF THIS ELEMENT IS HEADER OR MAIN FOR NEXT ITERATION THROUGH CALCULATEPARENT
		if (line.getName().equals("rssheader")) {
			initsearch : for (Element element : line.getElements()) {
				if (element.getName().equals("Finess")) {
					finess = element.getElement().toString();
					break initsearch;
				}
			}
			version = line.getVersion();
			wasHeader = true;
			wasMain = false;
			currentParent = null;
		} else if (line.getName().equals("rssmain")){
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
	public String getFiness() {
		return finess;
	}

	@Override
	public String getVersion() {
		return version;
	}
}
