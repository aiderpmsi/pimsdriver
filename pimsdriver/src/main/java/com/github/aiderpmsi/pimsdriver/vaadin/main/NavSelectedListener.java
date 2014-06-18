package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.PmsiContentPanel;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;

public class NavSelectedListener implements Listener {

	private static final long serialVersionUID = -2082906260394975620L;

	private PmsiContentPanel pcp;
	
	public NavSelectedListener(PmsiContentPanel pcp) {
		this.pcp = pcp;
	}

	@Override
	public void componentEvent(Event event) {
		// CHECK IF THE EVENT IS ONE NAVSELECTEDEVENT
		if (event.getClass() == NavSelectedEvent.class) {
			NavSelectedEvent nse = (NavSelectedEvent) event;
			pcp.show(nse.getType(), nse.getModel());
		}
		// IF ITS NOT, DO NOTHING
	}

}
