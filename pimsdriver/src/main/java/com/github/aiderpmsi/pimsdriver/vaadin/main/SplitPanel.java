package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.PmsiContentPanel;
import com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel.FinessComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

public class SplitPanel extends HorizontalSplitPanel{

	private static final long serialVersionUID = -5501213861687043638L;

	private PmsiContentPanel contentPanel;
	
	private FinessComponent finessPanel;
	
	@SuppressWarnings("unused")
	private SplitPanel() {};
	
	public SplitPanel(RootWindow rootWindow) {
		// CREATES THE SPLIT PANEL
		super();
		setSplitPosition(.25f, Unit.PERCENTAGE);
		addStyleName("pims-splitpanel");

		// CREATES THE SUB COMPONENTS
		contentPanel = new PmsiContentPanel();
		finessPanel = new FinessComponent(rootWindow);

		// CREATES AND FILLS THE LEFT AND RIGHT LAYOUTS
		VerticalLayout layout1 = new VerticalLayout(finessPanel);
		layout1.addStyleName("pims-splitpanel-leftlayout");
		setFirstComponent(layout1);
		VerticalLayout layout2 = new VerticalLayout(contentPanel);
		layout2.addStyleName("pims-splitpanel-rightlayout");
		setSecondComponent(layout2);

	}

	public PmsiContentPanel getContentPanel() {
		return contentPanel;
	}

	public FinessComponent getFinessPanel() {
		return finessPanel;
	}
}
