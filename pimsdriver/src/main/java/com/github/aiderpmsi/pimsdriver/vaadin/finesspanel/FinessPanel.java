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
		@SuppressWarnings("unchecked")
		Property<Integer> propsucessdepth = (Property<Integer>) hc.getContainerProperty(idsuccess, "depth");
		propsucessdepth.setValue(0);
		
		// ERRORS ROOT
		final Object idfail = hc.addItem();
		@SuppressWarnings("unchecked")
		Property<String> propfail = (Property<String>) hc.getContainerProperty(idfail, "caption");
		propfail.setValue("Fichiers en erreur");
		@SuppressWarnings("unchecked")
		Property<Integer> propfaildepth = (Property<Integer>) hc.getContainerProperty(idfail, "depth");
		propfaildepth.setValue(0);

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
				// IF WE EXPAND A ROOT NODE
				if ((Integer) hc.getContainerProperty(event.getItemId(), "depth").getValue() == 0) {
					String filter = (event.getItemId() == idsuccess ? "processed" : "failed");
					// FILL THE SUCCESS TREE
					List<String> finesses = (new NavigationDAO()).getFiness(filter);
					for (String finess : finesses) {
						Object id = hc.addItem();
						@SuppressWarnings("unchecked")
						Property<String> prop = (Property<String>) hc.getContainerProperty(id, "caption");
						prop.setValue(finess);
						@SuppressWarnings("unchecked")
						Property<Integer> depth = (Property<Integer>) hc.getContainerProperty(id, "depth");
						depth.setValue(1);
						hc.setParent(id, event.getItemId());
					}
				}
				// IF WE EXPAND A FINESS NODE
				else if ((Integer) hc.getContainerProperty(event.getItemId(), "depth").getValue() == 1) {
					String filter = (hc.getParent(event.getItemId()) == idsuccess ? "processed" : "failed");
					String finess = (String) hc.getContainerProperty(event.getItemId(), "caption").getValue();
					// FILL THE FINESS TREE :
					List<NavigationDAO.YM> yms = (new NavigationDAO()).getYM(filter, finess);
					// WHEN YMS IS NULL, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
					if (yms == null) {
						hc.removeItemRecursively(event.getItemId());
					} else {
						for (NavigationDAO.YM ym : yms) {
							Object id = hc.addItem();
							@SuppressWarnings("unchecked")
							Property<String> prop = (Property<String>) hc.getContainerProperty(id, "caption");
							prop.setValue(ym.year + " M" + ym.month);
							@SuppressWarnings("unchecked")
							Property<Integer> propyear = (Property<Integer>) hc.getContainerProperty(id, "year");
							propyear.setValue(ym.year);
							@SuppressWarnings("unchecked")
							Property<Integer> propmonth = (Property<Integer>) hc.getContainerProperty(id, "month");
							propmonth.setValue(ym.month);
							@SuppressWarnings("unchecked")
							Property<Integer> depth = (Property<Integer>) hc.getContainerProperty(id, "depth");
							depth.setValue(2);
							hc.setParent(id, event.getItemId());
						}
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
