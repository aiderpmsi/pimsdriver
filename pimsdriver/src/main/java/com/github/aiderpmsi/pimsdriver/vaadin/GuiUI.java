package com.github.aiderpmsi.pimsdriver.vaadin;

import com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi.ImportRsfModel;
import com.vaadin.annotations.Theme;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
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
			final Window wHelp = new ImportPmsiUpload();
	        
	        // DEFINE THE FORM CONTENT
	        ImportRsfModel model = new ImportRsfModel();
	        model.setDefaultValues();
	        final BeanFieldGroup<ImportRsfModel> binder =
	        		new BeanFieldGroup<ImportRsfModel>(ImportRsfModel.class);
	        binder.setItemDataSource(model);
	        binder.addCommitHandler(new Gestioncommit(fup));
	        
	        fl.addComponent(binder.buildAndBind("Année", "yearValue"));
	        fl.addComponent(binder.buildAndBind("Mois", "monthValue"));
	        fl.addComponent(binder.buildAndBind("Finess", "finessValue"));
	        
	        fl.addComponent(new Button("OK", new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						binder.commit();
					} catch (CommitException e) {
						new Notification(e.getMessage());
					}
					UI.getCurrent().removeWindow(wHelp);
	            }
	        }));
	        
	        subContent.addComponent(fl);
	        
			UI.getCurrent().addWindow(wHelp);
		}
		
	}
	
	private class Gestioncommit implements CommitHandler {

		private FileUploader fup;
		
		public Gestioncommit(FileUploader fup) {
			this.fup = fup;
		}
		
		@Override
		public void preCommit(CommitEvent commitEvent) throws CommitException {
			// VERIFY THAT EVERYTHING IS GOOG
			if (fup.getFilename() == null)
				throw new CommitException("Aucun fichier rsf choisi");
		}

		@Override
		public void postCommit(CommitEvent commitEvent) throws CommitException {
			// DO NOTHING
		}
		
	}
}
