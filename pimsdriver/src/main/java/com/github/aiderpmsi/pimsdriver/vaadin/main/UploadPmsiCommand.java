package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.vaadin.upload.UploadWindow;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class UploadPmsiCommand implements Command {

	private static final long serialVersionUID = -9046058057507224310L;
	
	@Override
	public void menuSelected(MenuItem selectedItem) {
		// CREATE WINDOW
		final Window wProcess = new UploadWindow();
		        
		UI.getCurrent().addWindow(wProcess);
	}
}
