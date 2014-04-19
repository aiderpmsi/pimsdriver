package com.github.aiderpmsi.pimsdriver.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Theme("pimsdriver")
public class GuiUI extends UI {

	/**
	 * Generated Serial
	 */
	private static final long serialVersionUID = 3109715875916629911L;

	@Override
	protected void init(VaadinRequest request) {
		initLayout();
	}
	
	private void initLayout() {
		// SETS THE HEIGHT ANT WIDTH OF THIS APPLICATION TO FULL
		setHeight(100F, Unit.PERCENTAGE);
		setWidth(100F, Unit.PERCENTAGE);
		
		// ROOT OF USER INTERFACE IS VERTICAL LAYOUT
		VerticalLayout layout = new VerticalLayout();
		layout.setStyleName("mylayout");
		layout.setSizeFull();
		setContent(layout);
		
		// HEADER
		Label header = new Label("PimsDriver");
		header.setStyleName("header");
		layout.addComponent(header);
		
		// MENUBAR
		MenuBar menubar = new MenuBar();
		menubar.setStyleName("mymenubar");
		MenuItem files = menubar.addItem("Fichiers", null, null);
		files.addItem("Ajouter Pmsi", null, new AddPmsi());
		files.addSeparator();
		files.addItem("Traitements Pmsi", null, new ProcessPmsi());	
		layout.addComponent(menubar);

		// ADDS A PANEL UNDER FILES
		Panel pan = new Panel();
		pan.setSizeFull();

		// THIS PANEL TAKES MAX SIZE
		layout.addComponent(pan);
		layout.setExpandRatio(pan, 1f);
		
		// ADDS A SPLIT PANEL
		HorizontalSplitPanel hsplit = new HorizontalSplitPanel();
		hsplit.setSizeFull();
		pan.setContent(hsplit);

		// ADDS A VERTICAL LAYOUT ON THE LEFT
		VerticalLayout leftPanelLayout = new VerticalLayout();
		hsplit.addComponent(leftPanelLayout);
		
		// ADD ALL FINESS WHICH HAVE BEEN PROCESSES
		Label l1 = new Label("There are no previously saved actions.");
		Label l2 = new Label("There are no saved notes.");
		Label l3 = new Label("There are currently no issues.");
		leftPanelLayout.addComponent(l1);
		leftPanelLayout.addComponent(l2);
		leftPanelLayout.addComponent(l3);
		
	}
	
	private class AddPmsi implements Command {

		private static final long serialVersionUID = 1315554704783116551L;

		@Override
		public void menuSelected(MenuItem selectedItem) {
			// CREATE WINDOW
			final Window wHelp = new PmsiUploadWindow();
	        
			UI.getCurrent().addWindow(wHelp);
		}
	}
	
	private class ProcessPmsi implements Command {

		private static final long serialVersionUID = -9046058057507224310L;

		@Override
		public void menuSelected(MenuItem selectedItem) {
			// CREATE WINDOW
			final Window wProcess = new PmsiProcessWindow();
	        
			UI.getCurrent().addWindow(wProcess);
		}
	}
}
