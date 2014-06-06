package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.report.ReportWindow;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class ReportPmsiCommand implements Command {

	/** Serial Id */
	private static final long serialVersionUID = 8387109743781055066L;
	
	private UploadedPmsi model;
	
	public ReportPmsiCommand(UploadedPmsi model) {
		this.model = model;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		// CREATE WINDOW
		final Window wReport = new ReportWindow(model);
				        
		UI.getCurrent().addWindow(wReport);
	}
	
	

}
