package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.util.Collection;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree;

public class FinessCollapseListener implements Tree.CollapseListener {

	private static final long serialVersionUID = -350653571633059983L;

	private HierarchicalContainer hc;
	
	@SuppressWarnings("unused")
	private FinessCollapseListener() { }
	
	public FinessCollapseListener(HierarchicalContainer hc) {
		this.hc = hc;
	}

	@Override
	public synchronized void nodeCollapse(CollapseEvent event) {
			// REMOVE ALL CHILDREN OF THIS COLLAPSING ITEM IF NOT NULL
			@SuppressWarnings("unchecked")
			Collection<Object> childrenCollection = (Collection<Object>) hc.getChildren(event.getItemId());
			if (childrenCollection != null) {
				for (Object child : childrenCollection.toArray()) {
					hc.removeItemRecursively(child);
				}
			}
		}

}
