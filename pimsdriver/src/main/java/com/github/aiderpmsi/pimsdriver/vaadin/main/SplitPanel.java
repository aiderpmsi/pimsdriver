package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.PmsiContentPanel;
import com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel.FinessComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

public class SplitPanel extends HorizontalSplitPanel{

	private static final long serialVersionUID = -5501213861687043638L;

	private final RootWindow rootWindow;

	private final PmsiContentPanel contentPanel;
	
	private final FinessComponent finessPanel;
		
	public SplitPanel(RootWindow rootWindow) {
		super();
		
		this.rootWindow = rootWindow;
		this.contentPanel = new PmsiContentPanel(this);
		this.finessPanel = new FinessComponent(this);
		
		setSplitPosition(.25f, Unit.PERCENTAGE);
		addStyleName("pims-splitpanel");

		// CREATES AND FILLS THE LEFT LAYOUT
		setFirstComponent(new VerticalLayout(finessPanel));
		getFirstComponent().addStyleName("pims-splitpanel-leftlayout");

		// CREATES AND FILLS THE RIGHT LAYOUT
		setSecondComponent(new VerticalLayout(contentPanel));
		getSecondComponent().addStyleName("pims-splitpanel-rightlayout");
	}

	public PmsiContentPanel getContentPanel() {
		return contentPanel;
	}

	public FinessComponent getFinessPanel() {
		return finessPanel;
	}

	public RootWindow getRootWindow() {
		return rootWindow;
	}
	
}
