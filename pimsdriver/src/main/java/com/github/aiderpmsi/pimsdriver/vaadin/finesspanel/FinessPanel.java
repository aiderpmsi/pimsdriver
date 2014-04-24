package com.github.aiderpmsi.pimsdriver.vaadin.finesspanel;

import java.util.Collection;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDAO;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.ExpandEvent;

public class FinessPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 5192397393504372354L;

	// TODO : IN ORDER TO ENSURE THAT THE LIST OF ELEMENTS IN PANEL IS UP TO DATE, USE POSTGRESQL WITH JSON

	public FinessPanel() {
		
		// SETS THE HIERARCHICAL CONTAINER PROPERTIES
		final HierarchicalContainer hc = new HierarchicalContainer();
		hc.addContainerProperty("caption", String.class, "");
		
		// SUCESS ROOT
		final Object idsuccess = hc.addItem();
		@SuppressWarnings("unchecked")
		Property<String> propsucess = (Property<String>) hc.getContainerProperty(idsuccess, "caption");
		propsucess.setValue("Finess");
		
		// ERRORS ROOT
		final Object idfail = hc.addItem();
		@SuppressWarnings("unchecked")
		Property<String> propfail = (Property<String>) hc.getContainerProperty(idfail, "caption");
		propfail.setValue("Fichiers en erreur");

		// TREE WIDGET
		Tree finessTree = new Tree();
		finessTree.setContainerDataSource(hc);
		finessTree.setItemCaptionPropertyId("caption");
		setContent(finessTree);
		
		finessTree.addExpandListener(new Tree.ExpandListener() {
			/** Generated serial id */
			private static final long serialVersionUID = 7235194562779113128L;
			@Override
			public void nodeExpand(ExpandEvent event) {
				if (event.getItemId() == idsuccess || event.getItemId() == idfail) {
					String filter = (event.getItemId() == idsuccess ? "processed" : "failed");
					// FILL THE SUCCESS TREE
					List<String> finesses = (new NavigationDAO()).getFiness(filter);
					for (String finess : finesses) {
						Object id = hc.addItem();
						@SuppressWarnings("unchecked")
						Property<String> prop = (Property<String>) hc.getContainerProperty(id, "caption");
						prop.setValue(finess);
						hc.setParent(id, event.getItemId());
					}
				}
			}
		});
		
		finessTree.addCollapseListener(new Tree.CollapseListener() {
			/** Generated serial id */
			private static final long serialVersionUID = -8083420762047096032L;
			public void nodeCollapse(CollapseEvent event) {
				if (event.getItemId() == idsuccess) {
					// REMOVE ALL CHILDREN OF THIS COLLAPSING ITEM
					@SuppressWarnings("unchecked")
					Collection<Object> children = (Collection<Object>) hc.getChildren(event.getItemId());
					for (Object child : children) {
						hc.removeItemRecursively(child);
					}
				}
			}
		});
	}
	
}
