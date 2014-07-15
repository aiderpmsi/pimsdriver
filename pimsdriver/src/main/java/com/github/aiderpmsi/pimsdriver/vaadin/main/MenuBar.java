package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;
import com.github.aiderpmsi.pimsdriver.vaadin.pending.PendingPmsiWindow;
import com.github.aiderpmsi.pimsdriver.vaadin.report.ReportWindow;
import com.github.aiderpmsi.pimsdriver.vaadin.upload.UploadWindow;
import com.vaadin.ui.UI;

public class MenuBar extends com.vaadin.ui.MenuBar {
	
	public enum MenuBarSelected {
		factures("Factures"),
		sejours("Séjours");
		private final String label;
		private MenuBarSelected(String label) {
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
	}

	private static final long serialVersionUID = 8541452935763539785L;

	private final RootWindow rootWindow;
	
	private final MenuItem files;
	
	private final MenuItem navigations;
	
	private final MenuItem rapports;
	
	public MenuBar(RootWindow rootWindow) {
		super();
		addStyleName("pims-main-menubar");

		this.rootWindow = rootWindow;
 
		files = addItem("Fichiers", null, null);
		files.addItem("Ajouter Pmsi", null,
				(selectedItem) ->  UI.getCurrent().addWindow(new UploadWindow(rootWindow.getMainApplication().getServletContext())));

		files.addSeparator();
		files.addItem("Traitements Pmsi", null,
				(selectedItem) ->  UI.getCurrent().addWindow(new PendingPmsiWindow(rootWindow.getMainApplication().getServletContext())));
		
		rapports = addItem("Rapports", null, null);
		rapports.setVisible(false);
		
		navigations = addItem("Navs", null, null);
		navigations.setVisible(false);
	}

	public void setUpload(final UploadedPmsi model) {
		// FIRST, CLEANUP NAVIGATION AND REPORTS MENU ITEMS
		navigations.removeChildren();
		rapports.removeChildren();
		
		// IF NEW MODEL, CREATE THE LINKS
		if (model != null && model.getStatus() != null && model.getStatus() == Status.successed) {
			navigations.addItem("Factures", null,
					(selectedItem) -> rootWindow.setMenuNavigationSelected(model, MenuBarSelected.factures));
			navigations.addItem("Séjours", null,
					(selectedItem) -> rootWindow.setMenuNavigationSelected(model, MenuBarSelected.sejours));
			navigations.setVisible(true);

			rapports.addItem("Factures", null,
					(selectedItem) -> UI.getCurrent().addWindow(new ReportWindow(model, ReportWindow.Category.factures)));
			rapports.addItem("Séjours", null,
					(selectedItem) -> UI.getCurrent().addWindow(new ReportWindow(model, ReportWindow.Category.sejours)));
			rapports.setVisible(true);
		} else {
			navigations.setVisible(false);
			rapports.setVisible(false);
		}
	}
	
}
