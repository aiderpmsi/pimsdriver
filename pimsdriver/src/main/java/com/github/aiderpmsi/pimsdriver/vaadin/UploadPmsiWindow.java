package com.github.aiderpmsi.pimsdriver.vaadin;

import com.github.aiderpmsi.pimsdriver.dao.ImportPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dao.TransactionException;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadElementModel;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.FileUploader;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

public class UploadPmsiWindow extends Window {

	private static final long serialVersionUID = -2583394688969956613L;
	
	private FileUploader rsf;
	private FileUploader rss;

	public UploadPmsiWindow() {
		// TITLE
		super("Ajouter un fichier Pmsi");
		
		// SET VISUAL ASPECT
        setWidth("650px");
        setClosable(true);
        setResizable(true);
        setModal(true);
        setStyleName("addpmsi");
        center();

        // SELECT LAYOUT
        FormLayout fl = new FormLayout();
        setContent(fl);
        
        // ADD RSF FILE PICKER
        rsf = new FileUploader("rsf", this);
        Upload rsfUp = new Upload("RSF ", rsf);
        rsfUp.setImmediate(true);
        rsfUp.setButtonCaption("Téléverser");
        fl.addComponent(rsfUp);
        
        // ADD FILE UPLOADED
        TextField rsfFeedBack = new TextField("Fichier téléversé");
        rsfFeedBack.setEnabled(false);
        rsf.setFeedback(rsfFeedBack);
        fl.addComponent(rsfFeedBack);
        
        // ADD RSS FILE PICKER
        rss = new FileUploader("rss", this);
        Upload rssUp = new Upload("RSS ", rss);
        rssUp.setImmediate(true);
        rssUp.setButtonCaption("Téléverser");
        fl.addComponent(rssUp);
        
        // ADD FILE UPLOADED
        TextField rssFeedBack = new TextField("Fichier téléversé");
        rssFeedBack.setEnabled(false);
        rss.setFeedback(rssFeedBack);
        fl.addComponent(rssFeedBack);
        
        // ADD FORM FIELDS (FINESS, YEAR AND MONTH)
        PmsiUploadElementModel model = new PmsiUploadElementModel();
        model.initDefaultValues();
        final BeanFieldGroup<PmsiUploadElementModel> binder =
        		new BeanFieldGroup<PmsiUploadElementModel>(PmsiUploadElementModel.class);
        binder.setItemDataSource(model);

        TextField yearField = binder.buildAndBind("Année", "year", TextField.class);
        fl.addComponent(yearField);
        TextField monthField = binder.buildAndBind("Mois", "month", TextField.class);
        fl.addComponent(monthField);
        TextField finessField = binder.buildAndBind("Finess", "finess", TextField.class);
        fl.addComponent(finessField);
        
        // ADD VALIDATOR
        binder.addCommitHandler(new CommitManager());
        Button okButton = new Button("Valider");
        okButton.addClickListener(new Validate(binder, this));
        fl.addComponent(okButton);
        
        // ADD CLOSE LISTENER
        addCloseListener(new CustomCloseListener());
	}

	/**
	 * Manages the closing of the window (release the uploaded files)
	 * @author delabre
	 * 
	 */
	private class CustomCloseListener implements Window.CloseListener {

		private static final long serialVersionUID = -983340052018092722L;

		@Override
		public void windowClose(CloseEvent e) {
			// DELETES THE UPLOADED FILES
			rss.release();
			rsf.release();
		}
		
	}
	
	/**
	 * Manages the handling of datas when committing
	 * @author delabre
	 *
	 */
	private class CommitManager implements CommitHandler {

		private static final long serialVersionUID = 1L;
		
		@Override
		public void preCommit(CommitEvent commitEvent) throws CommitException {
			// TEST IF ONE RSF HAS BEEN AT LEAST UPLOADED
			if (rsf.getFilename() == null)
				throw new CommitException("Un fichier RSF doit au moins être téléversé");
			// TEST IF BINDER HAS NO ERROR
			if (!commitEvent.getFieldBinder().isValid())
				throw new CommitException("Il y a des erreurs dans le formulaire");
		}

		@Override
		public void postCommit(CommitEvent commitEvent) throws CommitException {
			// NOTHING TO DO
		}
		
	}
	
	private class Validate implements Button.ClickListener {
		
		private static final long serialVersionUID = 1L;
		private BeanFieldGroup<PmsiUploadElementModel> binder;
		private Window uploadWindow;
		
		public Validate(BeanFieldGroup<PmsiUploadElementModel> binder, Window uploadWindow) {
			this.binder = binder;
			this.uploadWindow = uploadWindow;
		}
		
		@Override
		public void buttonClick(ClickEvent event) {
			try {
				binder.commit();

				// NO ERROR : STORE THE ELEMENTS
				PmsiUploadElementModel model = binder.getItemDataSource().getBean();
				
				try {
					new ImportPmsiDTO().importPmsi(
							model,
							rsf.getFile(),
							(rss.getFilename() == null ? null : rss.getFile()));
				} catch (TransactionException e) {
					throw new CommitException(e);
				}
				
				UI.getCurrent().removeWindow(uploadWindow);
				Notification.show("Fichier(s) téléversés", Notification.Type.WARNING_MESSAGE);
			} catch (CommitException e) {
				String message = e.getCause().getMessage();
				if (message.length() == 0)
					message = e.getMessage();
				Notification.show(message, Notification.Type.WARNING_MESSAGE);
			}
		}
	}

}
