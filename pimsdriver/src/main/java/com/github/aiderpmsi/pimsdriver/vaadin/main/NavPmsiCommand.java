package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class NavPmsiCommand implements Command {

	/** Serial Id */
	private static final long serialVersionUID = 8387109743781055066L;
	
	private NavSelectedEvent.Type type;
	
	private RootWindow rootWindow;
	
	private UploadedPmsi model;
	
	public NavPmsiCommand(UploadedPmsi model, NavSelectedEvent.Type type, RootWindow rootWindow) {
		this.type = type;
		this.rootWindow = rootWindow;
		this.model = model;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		//  PREVENT GUIUI THAT A NAV HAS BEEN SELECTED
		rootWindow.fireNavPmsiSelected(type, model);
	}
	
	

}
