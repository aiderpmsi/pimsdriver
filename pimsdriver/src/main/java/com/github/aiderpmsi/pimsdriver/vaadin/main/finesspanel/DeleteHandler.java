package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import com.github.aiderpmsi.pimsdriver.dao.ImportPmsiDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;

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
				PmsiUploadedElementModel model = (PmsiUploadedElementModel) hc.getContainerProperty(target, "model").getValue();
				// DELETES THE UPLOAD
				(new ImportPmsiDTO()).deleteUpload(model.getRecordId());
				// REMOVE THE ITEM
				fp.removeItem(target);
			}
		}
	}
}
