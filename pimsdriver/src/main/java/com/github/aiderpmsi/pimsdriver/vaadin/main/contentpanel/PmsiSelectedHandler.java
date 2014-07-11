package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.github.aiderpmsi.pimsdriver.vaadin.main.MenuBar;
import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsidetails.PmsiDetailsWindow;
import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsisource.PmsiSourceWindow;
import com.vaadin.event.Action;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class PmsiSelectedHandler implements Action.Handler {
	
	private static final long serialVersionUID = 1L;

	static final Action ACTION_DETAILS = new Action("détails");
	static final Action ACTION_SOURCE = new Action("source");
	static final Action[] ACTIONS = new Action[] { ACTION_DETAILS, ACTION_SOURCE };
	static final Action[] NO_ACTION = new Action[] {};
	
	private final LazyQueryContainer lzq;
	
	private final MenuBar.MenuBarSelected type;
	
	private final Long pmel_root;
	
	public PmsiSelectedHandler(final MenuBar.MenuBarSelected type, final LazyQueryContainer lzq, final Long pmel_root) {
		this.lzq = lzq;
		this.type = type;
		this.pmel_root = pmel_root;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (target != null) {
			return ACTIONS;
		} else {
			return NO_ACTION;
		}
	}
	
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		// GETS THE POSITION ID
		Long pmel_position = (Long) lzq.getContainerProperty(target, "pmel_position").getValue();

		if (target != null) {
			switch (type) {
			case factures:
				String numfacture = (String) lzq.getContainerProperty(target, "numfacture").getValue();
				if (action.equals(ACTION_DETAILS)) {
					// SHOWS FACT DETAILS
					final Window wProcess = new PmsiDetailsWindow(pmel_root, pmel_position, type, numfacture);
					UI.getCurrent().addWindow(wProcess);
				} else if (action.equals(ACTION_SOURCE)) {
					// SHOWS FACT DETAILS
					UI.getCurrent().addWindow(new PmsiSourceWindow(pmel_root, pmel_position, type, numfacture));
				}
				break;
			case sejours:
				String numrum = (String) lzq.getContainerProperty(target, "numrum").getValue();
				if (action.equals(ACTION_DETAILS)) {
					// SHOWS FACT DETAILS
					final Window wProcess = new PmsiDetailsWindow(pmel_root, pmel_position, type, numrum);
					UI.getCurrent().addWindow(wProcess);
				} else if (action.equals(ACTION_SOURCE)) {
					// SHOWS FACT DETAILS
					final Window wProcess = new PmsiSourceWindow(pmel_root, pmel_position, type, numrum);
					UI.getCurrent().addWindow(wProcess);
				}
				break;
			}
		}
	}

}
