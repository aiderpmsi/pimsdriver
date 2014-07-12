package com.github.aiderpmsi.pimsdriver.vaadin.upload;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.IOActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.FileUploader;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class OKListener implements Button.ClickListener {
		
	private static final long serialVersionUID = 7358663451648716300L;

	private BeanFieldGroup<UploadPmsi> binder;
	private Window uploadWindow;
	private FileUploader rsf, rss;
	
	private final ServletContext context;
	
	public OKListener(BeanFieldGroup<UploadPmsi> binder,
			Window uploadWindow, FileUploader rsf,
			FileUploader rss,
			final ServletContext context) {
		this.binder = binder;
		this.uploadWindow = uploadWindow;
		this.rsf = rsf;
		this.rss = rss;
		this.context = context;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		try {
			// BIND THE ENTRIES WITH THE BEAN
			binder.commit();

			// NO ERROR : STORE THE ELEMENTS
			UploadPmsi model = binder.getItemDataSource().getBean();
			
			try {
				(new IOActions(context)).uploadPmsi(model, rsf.getFile(), rss.getFile());
			} catch (ActionException e) {
				throw new CommitException(e);
			}
			
			UI.getCurrent().removeWindow(uploadWindow);
			Notification.show("Fichier(s) téléversés", Notification.Type.WARNING_MESSAGE);
		} catch (CommitException e) {
			Notification.show("Erreur de chargement du fichier", Notification.Type.WARNING_MESSAGE);
		}
	}

}
