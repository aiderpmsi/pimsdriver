package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class RootWindow extends CssLayout {

	/** Generated Serial Id */
	private static final long serialVersionUID = -8909569781887366046L;

	private final Label header;
	
	private final MenuBar menuBar;

	private final SplitPanel splitPanel;

	private final MainApplication mainApplication;

	public RootWindow(MainApplication mainApplication) {
		super();
		this.mainApplication = mainApplication;

		addStyleName("pims-main-layout");

		header = new Label("pimsdriver");
		header.addStyleName("pims-main-header");
		addComponent(header);
		
		menuBar = new MenuBar(this);
		addComponent(menuBar);

		splitPanel = new SplitPanel(this);
		addComponent(splitPanel);
	}
	
	public void setUploadSelected(final UploadedPmsi model) {
		splitPanel.getContentPanel().setUpload(model);
		menuBar.setUpload(model);
	}
	
	public void setMenuNavigationSelected(final UploadedPmsi model, final MenuBar.MenuBarSelected type) {
		splitPanel.getContentPanel().show(type, model);
	}

	public Label getHeader() {
		return header;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public SplitPanel getSplitPanel() {
		return splitPanel;
	}

	public MainApplication getMainApplication() {
		return mainApplication;
	}
	
}
