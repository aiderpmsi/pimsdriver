package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.IOActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Notification;

public class DeleteHandler implements Action.Handler {
	
	private static final long serialVersionUID = 1L;

	static final Action ACTION_DELETE = new Action("delete");
	static final Action[] ACTIONS = new Action[] { ACTION_DELETE };
	static final Action[] NO_ACTION = new Action[] {};
	
	private HierarchicalContainer hc;
	
	private FinessPanel fp;
	
	@SuppressWarnings("unused")
	private DeleteHandler() {}
	
	public DeleteHandler(HierarchicalContainer hc, FinessPanel fp) {
		this.hc = hc;
		this.fp = fp;
	}
	
	public Action[] getActions(Object target, Object sender) {
		// WHEN TARGET IS ONE ITEM ID, CHECK IF IT IS AT DEPTH 3
		if (target != null) {
			Integer depth = (Integer) hc.getContainerProperty(target, "depth").getValue();
			if (depth == 3) {
				return ACTIONS;
			}
		}
		return NO_ACTION;
	}
	
	public void handleAction(Action action, Object sender, Object target) {
		// CHECK THAT THIS TARGET HAS DEPTH 3
		if (target != null) {
			Integer depth = (Integer) hc.getContainerProperty(target, "depth").getValue();
			if (depth == 3) {
				// GETS THE ASSOCIATED MODEL
				UploadedPmsi model = (UploadedPmsi) hc.getContainerProperty(target, "model").getValue();
				// DELETES THE UPLOAD
				IOActions ioActions = new IOActions();
				try {
					ioActions.deletePmsi(model);
					// REMOVE THE ITEM
					fp.removeItem(target);
				} catch (ActionException e) {
					Notification.show("Erreur de suppression du fichier", Notification.Type.WARNING_MESSAGE);
				}
			}
		}
	}
}
