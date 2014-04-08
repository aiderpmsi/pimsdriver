package com.github.aiderpmsi.pimsdriver.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
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
		setContent(layout);
		
		// HEADER
		Label header = new Label();
		header.setCaption("PimsDriver");
		header.setStyleName("header");
		layout.addComponent(header);
		
		// MENUBAR
		MenuBar menubar = new MenuBar();
		menubar.setStyleName("mymenubar");
		MenuItem files = menubar.addItem("Fichiers", null, null);
		files.addItem("Ajouter Pmsi", null, new AddPmsi());
		files.addSeparator();
		files.addItem("Traitements Pmsi", null, null);	
		layout.addComponent(menubar);
		
		
		// ADD 3 LITTLE LABEL
		for (int i = 0 ; i < 3 ; i++) {
			Label label = new Label();
			label.setCaption("Contenu " + Integer.toString(i) + " ...");
			layout.addComponent(label);
		}
		
	}
	
	private class AddPmsi implements Command {

		/**
		 * Generated serial id
		 */
		private static final long serialVersionUID = 1315554704783116551L;

		@Override
		public void menuSelected(MenuItem selectedItem) {
			// CREATE WINDOW
			final Window wHelp = new Window("Ajouter un fichier Pmsi");
	        wHelp.setWidth("650px");
	        wHelp.setClosable(false);
	        wHelp.setResizable(false);
	        wHelp.setModal(true);
	        wHelp.setStyleName("addpmsi");
	        wHelp.center();

	        // SELECT LAYOUT
	        VerticalLayout subContent = new VerticalLayout();
	        subContent.setMargin(true);
	        wHelp.setContent(subContent);

	        // SETS CONTENT
	        Label label = new Label();
			label.setCaption("Contenu");
			subContent.addComponent(label);

			// CLOSE BUTTON
			Button button = new Button("Fermer", new Button.ClickListener() {
				private static final long serialVersionUID = 2959413044468131885L;
				@Override
				public void buttonClick(ClickEvent event) {
					UI.getCurrent().removeWindow(wHelp);
				}
			});
			subContent.addComponent(button);
			UI.getCurrent().addWindow(wHelp);
		}
		
	}
}
