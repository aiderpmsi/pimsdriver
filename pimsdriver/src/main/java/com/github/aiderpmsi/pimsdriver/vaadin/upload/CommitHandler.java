package com.github.aiderpmsi.pimsdriver.vaadin.upload;

import com.github.aiderpmsi.pimsdriver.vaadin.utils.FileUploader;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;

public class CommitHandler  implements com.vaadin.data.fieldgroup.FieldGroup.CommitHandler {
	
	private static final long serialVersionUID = 6714428459661279802L;

	private FileUploader rsf;
	
	public CommitHandler(FileUploader rsf, FileUploader rss) {
		this.rsf = rsf;
	}
	
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
