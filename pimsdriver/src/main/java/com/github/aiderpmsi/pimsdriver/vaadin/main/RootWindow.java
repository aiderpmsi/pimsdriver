package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("pimsdriver")
public class RootWindow extends UI {

	/** Generated Serial */
	private static final long serialVersionUID = 3109715875916629911L;

	/** Layout */
	private Layout layout;
	
	private SplitPanel splitPanel;
	
	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		layout = createLayout();
		layout.setSizeFull();
		setContent(layout);
		
		// ADDS EACH WIDGET INSIDE LAYOUT
		Header header = new Header();
		layout.addComponent(header);
		MenuBar menuBar = new MenuBar(this);
		layout.addComponent(menuBar);
		splitPanel = new SplitPanel(this);
		layout.addComponent(splitPanel);
		// SPLIT PANEL EXPANDS MAX If LAYOUT IS VERTICAL LAYOUT
		if (layout instanceof VerticalLayout)
			((VerticalLayout) layout).setExpandRatio(splitPanel, 1f);

		// REGISTER A FINESS SELECTED LISTENER WHEN A FINESS CHANGES
		addListener(new FinessSelectedListener(splitPanel.getContentPanel(), menuBar));
		// REGISTER A NAV SELECTED LISTENER WHEN WE WANT TO NAVIGATE THROUGH FACTURES OR SEJOURS
		addListener(new NavSelectedListener(splitPanel.getContentPanel()));
	}
	
	private Layout createLayout() {
		// CREATES THE LAYOUT
		Layout thisLayout = new VerticalLayout();
		thisLayout.addStyleName("pims-main-layout");
		return thisLayout;
	}		
	
	public void fireFinessSelected(UploadedPmsi model, UploadedPmsi.Status status) {
		FinessSelectedEvent fse = new FinessSelectedEvent(this);
		fse.setModel(model);
		fse.setStatus(status);
		fireEvent(fse);
	}
	
	public void fireNavPmsiSelected(NavSelectedEvent.Type type, UploadedPmsi model) {
		NavSelectedEvent nse = new NavSelectedEvent(type, model, this);
		fireEvent(nse);
	}
}
