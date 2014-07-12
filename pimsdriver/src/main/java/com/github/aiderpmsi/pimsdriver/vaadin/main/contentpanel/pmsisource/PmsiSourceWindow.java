package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsisource;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.vaadin.main.MenuBar;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PmsiSourceWindow extends Window {

	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public PmsiSourceWindow(final Long pmel_root,
			final Long pmel_position,
			final MenuBar.MenuBarSelected type,
			final String numpmsi,
			final ServletContext context) {
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

        final String stringContent = ActionEncloser.execute((exception) -> "Erreur de lecture du contenu source pmsi", 
        		() -> new NavigationActions(context).getPmsiSource(pmel_root, pmel_position));

        // ADDS TEXT FIELD
        final TextArea content = new TextArea("PMSI Source : ", stringContent);
        content.setReadOnly(true);
        content.setWordwrap(false);
        content.setSizeFull();
	        
        layout.addComponent(content);

	}

}
