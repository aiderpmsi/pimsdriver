package com.github.aiderpmsi.pimsdriver.vaadin;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Item;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
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
		
		// ROOT OF USER INTERFACE IS VERTICAL LAYOUT
		VerticalLayout layout = new VerticalLayout();
		layout.setStyleName("mylayout");
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
		
		
		// ADD 3 LITTLE LABEL
		for (int i = 0 ; i < 3 ; i++) {
			Label label = new Label();
			label.setCaption("Contenu " + Integer.toString(i) + " ...");
			layout.addComponent(label);
		}
		
		final Table table = new Table("The Brightest Stars");
        
		// Define two columns for the built-in container
		table.addContainerProperty("Name", String.class, null);
		table.addContainerProperty("Mag",  Float.class, null);

		// Add a row the hard way
		Object newItemId = table.addItem();
		Item row1 = table.getItem(newItemId);
		row1.getItemProperty("Name").setValue("Sirius");
		row1.getItemProperty("Mag").setValue(-1.46F);
		        
		// Add a few other rows using shorthand addItem()
		table.addItem(new Object[]{"Canopus",        -0.72F}, 2);
		table.addItem(new Object[]{"Arcturus",       -0.04F}, 3);
		table.addItem(new Object[]{"Alpha Centauri", -0.01F}, 4);
		        
		// Show 5 rows
		table.setPageLength(5);
		
		layout.addComponent(table);
		
        Table processtable = new Table("Pmsi en cours de traitement");
        LazyQueryContainer lqc = new LazyQueryContainer(
				new LazyQueryDefinition(false, 1000, "recordId"),
				new PmsiProcessQueryFactory());
        lqc.addContainerProperty("finess", String.class, "", true, true);
        lqc.addContainerProperty("year", Integer.class, null, true, true);
        processtable.setContainerDataSource(lqc);
        processtable.setVisibleColumns(new Object[] {"finess", "year"});
        processtable.setColumnHeaders(new String[] {"Finess", "Année"} );
        processtable.setSelectable(true);
        processtable.setPageLength(5);
        
       layout.addComponent(processtable);

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
