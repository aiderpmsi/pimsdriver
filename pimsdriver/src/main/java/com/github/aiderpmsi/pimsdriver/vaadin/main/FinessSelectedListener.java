package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.PmsiContentPanel;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;

public class FinessSelectedListener implements Listener {

	private static final long serialVersionUID = -2082906260394975620L;

	private PmsiContentPanel pcp;
	
	private MenuBar menuBar;
	
	public FinessSelectedListener(PmsiContentPanel pcp, MenuBar menuBar) {
		this.pcp = pcp;
		this.menuBar = menuBar;
	}

	@Override
	public void componentEvent(Event event) {
		// CHECK IF THE EVENT IS ONE FINESSSELECTEDEVENT
		if (event.getClass() == FinessSelectedEvent.class) {
			FinessSelectedEvent fse = (FinessSelectedEvent) event;
			pcp.setUpload(fse.getModel(), fse.getStatus());
			menuBar.setUpload(fse.getModel(), fse.getStatus());
		}
		// IF ITS NOT, DO NOTHING
	}

}
