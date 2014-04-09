package com.github.aiderpmsi.pimsdriver.vaadin;

import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ImportPmsiUpload extends Window {

	private static final long serialVersionUID = -2583394688969956613L;
	private FileUploader rsf;
	private FileUploader rss;

	public ImportPmsiUpload() {
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
        VerticalLayout vl = new VerticalLayout();
        setContent(vl);
        
        // ADD RSF FILE PICKER
        rsf = new FileUploader("rsf");
        Upload rsfUp = new Upload("RSF ", rsf);
        rsfUp.setButtonCaption("Téléverser");
        vl.addComponent(rsfUp);
        
        // ADD FILE UPLOADED
        TextField rsfFeedBack = new TextField("Fichier téléversé");
        rsf.setFeedback(rsfFeedBack);
        vl.addComponent(rsfFeedBack);
        
        // ADD RSS FILE PICKER
        rss = new FileUploader("rss");
        Upload rssUp = new Upload("RSS ", rss);
        rssUp.setButtonCaption("Téléverser");
        vl.addComponent(rssUp);
        
        // ADD FILE UPLOADED
        TextField rssFeedBack = new TextField("Fichier téléversé");
        rss.setFeedback(rssFeedBack);
        vl.addComponent(rssFeedBack);
	}

}
