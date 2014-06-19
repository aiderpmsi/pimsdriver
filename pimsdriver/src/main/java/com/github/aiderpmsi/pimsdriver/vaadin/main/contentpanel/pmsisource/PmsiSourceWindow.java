package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsisource;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.vaadin.main.NavSelectedEvent.Type;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PmsiSourceWindow extends Window {

	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public PmsiSourceWindow(Long pmel_root, Long pmel_position, Type type, String numpmsi) {
		// TITLE
		super(type.getLabel() + " : " + numpmsi);

		// SET VISUAL ASPECT
        setWidth("650px");
        setHeight("80%");
        setClosable(true);
        setResizable(true);
        setModal(true);
        setStyleName("details-factures");
        center();

        // SELECT LAYOUT
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

		try {
	        String stringContent = new NavigationActions().getPmsiSource(pmel_root, pmel_position);

	        // ADDS TEXT FIELD
	        TextArea content = new TextArea("PMSI Source :", stringContent);
	        content.setReadOnly(true);
	        content.setWordwrap(false);
	        content.setSizeFull();
	        
	        layout.addComponent(content);
	        
		} catch (ActionException e) {
			Notification.show("Erreur de lecture du contenu source pmsi", Notification.Type.WARNING_MESSAGE);
		}
        
	}

}
