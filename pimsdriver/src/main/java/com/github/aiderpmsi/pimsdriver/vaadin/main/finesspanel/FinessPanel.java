package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import com.github.aiderpmsi.pimsdriver.db.vaadin.query.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.RootWindow;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree;

public class FinessPanel extends Tree {

	/** Generated serial id */
	private static final long serialVersionUID = 5192397393504372354L;
		
	public FinessPanel(RootWindow rootElement) {
	
		super();

		// SETS THE HIERARCHICAL CONTAINER PROPERTIES
		HierarchicalContainer hc = new HierarchicalContainer();
		hc.addContainerProperty("caption", String.class, "");
		hc.addContainerProperty("finess", String.class, null);
		hc.addContainerProperty("depth", Integer.class, null);
		hc.addContainerProperty("year", Integer.class, null);
		hc.addContainerProperty("month", Integer.class, null);
		hc.addContainerProperty("status", UploadedPmsi.Status.class, null);
		hc.addContainerProperty("model", UploadedPmsi.class, null);
		
		// FILLS THE ROOT ELEMENTS FROM ELEMENTS LIST
		@SuppressWarnings("unchecked")
		Entry<Object, Object> entries[] = new Entry[3];
		entries[0] = new Entry<Object, Object>("depth", new Integer(0));
		entries[1] = new Entry<Object, Object>("caption", null);
		entries[2] = new Entry<Object, Object>("status", null);
		for (UploadedPmsi.Status status : UploadedPmsi.Status.values()) {
			if (status != UploadedPmsi.Status.pending) {
				entries[1].b = status.getLabel();
				entries[2].b = status;
				createContainerItemNode(hc, entries);
			}
		}

		setContainerDataSource(hc);
		setItemCaptionPropertyId("caption");
		setImmediate(true);
		
		// ADDS THE LISTENERS
		ExpandListener el = new FinessExpandListener(hc, this);
		CollapseListener cl = new FinessCollapseListener(hc);
		ItemClickListener icl = new ItemClickListener(rootElement, hc);

		addExpandListener(el);
		addCollapseListener(cl);
		addItemClickListener(icl);

		// ADD THE ACTION HANDLERS
		addActionHandler(new DeleteHandler(hc, this));
		
	}

	@SuppressWarnings("unchecked")
	public final Object createContainerItemNode(HierarchicalContainer hc, Entry<?, ?> ... entries) {

		Object itemId = hc.addItem();
		
		for (Entry<?, ?> entry : entries) {
			hc.getContainerProperty(itemId, entry.a).setValue(entry.b);
		}
		
		return itemId;
	}

	
	public void removeContainerItem(HierarchicalContainer hc, Object itemId) {

		// GETS THE PARENT
		Object parentId = hc.getParent(itemId);

		// REMOVE THIS ITEM
		hc.removeItemRecursively(itemId);

		// RECURSIVELY REMOVE PARENT ITEM IF IT HAS NO CHILDREN (ANT IT IS NOT ROOT)
		if (parentId != null && !hc.isRoot(parentId)) {
			if (hc.getChildren(parentId).size()  == 0) {
				removeContainerItem(hc, parentId);
			}
		}
	}
}
