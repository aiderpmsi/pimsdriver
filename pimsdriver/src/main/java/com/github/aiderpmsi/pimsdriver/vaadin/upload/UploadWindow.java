package com.github.aiderpmsi.pimsdriver.vaadin.upload;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.FileUploader;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

public class UploadWindow extends Window {

	private static final long serialVersionUID = -2583394688969956613L;
	
	private FileUploader rsf;
	private FileUploader rss;

	public UploadWindow(ServletContext context) {
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
        UploadPmsi model = new UploadPmsi();
        model.initDefaultValues();
        final BeanFieldGroup<UploadPmsi> binder =
        		new BeanFieldGroup<UploadPmsi>(UploadPmsi.class);
        binder.setItemDataSource(model);

        TextField yearField = binder.buildAndBind("Année", "year", TextField.class);
        fl.addComponent(yearField);
        TextField monthField = binder.buildAndBind("Mois", "month", TextField.class);
        fl.addComponent(monthField);
        TextField finessField = binder.buildAndBind("Finess", "finess", TextField.class);
        fl.addComponent(finessField);
        
        // ADD VALIDATOR
        binder.addCommitHandler(new com.github.aiderpmsi.pimsdriver.vaadin.upload.CommitHandler(rsf, rss));
        Button okButton = new Button("Valider");
        okButton.addClickListener(new OKListener(binder, this, rsf, rss, context));
        fl.addComponent(okButton);
        
        // ADD CLOSE LISTENER
        addCloseListener(new com.github.aiderpmsi.pimsdriver.vaadin.upload.CloseListener(rsf, rss));
	}

}
