package com.github.aiderpmsi.pimsdriver.vaadin.main;


public class MenuBar extends com.vaadin.ui.MenuBar {

	private static final long serialVersionUID = 8541452935763539785L;

	public MenuBar() {
		super();
		addStyleName("pims-main-menubar");
	
		MenuItem files = addItem("Fichiers", null, null);
		files.addItem("Ajouter Pmsi", null, new UploadPmsiCommand());
		files.addSeparator();
		files.addItem("Traitements Pmsi", null, new PendingPmsiCommand());
	}
}
