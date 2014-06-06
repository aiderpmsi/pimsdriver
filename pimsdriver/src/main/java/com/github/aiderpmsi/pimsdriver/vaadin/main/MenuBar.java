package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;

public class MenuBar extends com.vaadin.ui.MenuBar {

	private static final long serialVersionUID = 8541452935763539785L;

	private MenuItem files = null;
	
	private MenuItem navigations = null;
	
	private MenuItem rapports = null;
	
	public MenuBar() {
		super();
		addStyleName("pims-main-menubar");
	
		files = addItem("Fichiers", null, null);
		files.addItem("Ajouter Pmsi", null, new UploadPmsiCommand());
		files.addSeparator();
		files.addItem("Traitements Pmsi", null, new PendingPmsiCommand());
		
	}

	public void setUpload(UploadedPmsi model, Status status) {
		// FIRST CLEANUP
		if (navigations != null) {
			removeItem(navigations);
			navigations = null;
		}
		if (rapports != null) {
			removeItem(rapports);
			rapports = null;
		}
		// IF NEW MODEL, CREATE THE LINKS
		if (model != null) {
			navigations = addItem("Navs", null, null);
			navigations.addItem("Factures", null, null);
			navigations.addItem("Séjours", null, null);
			
			rapports = addItem("Rapports", null, null);
			rapports.addItem("Factures", null, new ReportPmsiCommand(model));
			rapports.addItem("Séjours", null, null);
		} else {
		}
	}
}
