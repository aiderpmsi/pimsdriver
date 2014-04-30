package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.PmsiContentPanel;
import com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel.FinessPanel;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

public class SplitPanel extends HorizontalSplitPanel{

	private static final long serialVersionUID = -5501213861687043638L;

	private PmsiContentPanel contentPanel;
	
	private FinessPanel finessPanel;
	
	public SplitPanel(RootWindow rootWindow) {
		// CREATES THE SPLIT PANEL
		super();
		setSplitPosition(.25f, Unit.PERCENTAGE);

		// CREATES THE SUB COMPONENTS
		contentPanel = new PmsiContentPanel();
		finessPanel = new FinessPanel(rootWindow);

		// CREATES AND FILLS THE LEFT AND RIGHT LAYOUTS
		setFirstComponent(new VerticalLayout(finessPanel));
		setSecondComponent(new VerticalLayout(contentPanel));

	}

	public PmsiContentPanel getContentPanel() {
		return contentPanel;
	}

	public FinessPanel getFinessPanel() {
		return finessPanel;
	}
}
