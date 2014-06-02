package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.util.Collection;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.RootWindow;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

public class FinessPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 5192397393504372354L;
	
	private static final Object[][] rootElts =
			new Object[][] {
		new Object[] {"Finess", UploadedPmsi.Status.successed},
		new Object[] {"Fichiers en erreur", UploadedPmsi.Status.failed}
	};
	
	private Tree finessTree;
	
	private HierarchicalContainer hc;
	
	@SuppressWarnings("unused")
	private FinessPanel() {};
	
	public FinessPanel(RootWindow rootElement) {
	
		super();
		setCaption(null);
		addStyleName("pims-finesspanel");
		
		// SETS THE HIERARCHICAL CONTAINER PROPERTIES
		hc = new HierarchicalContainer();
		hc.addContainerProperty("caption", String.class, "");
		hc.addContainerProperty("finess", String.class, null);
		hc.addContainerProperty("depth", Integer.class, null);
		hc.addContainerProperty("year", Integer.class, null);
		hc.addContainerProperty("month", Integer.class, null);
		hc.addContainerProperty("status", UploadedPmsi.Status.class, null);
		hc.addContainerProperty("model", UploadedPmsi.class, null);
		
		// FILLS THE ROOT ELEMENTS FROM ELEMENTS LIST
		for (Object[] rootElt : rootElts) {
			Object idItem = hc.addItem();
			@SuppressWarnings("unchecked")
			Property<String> captionProperty = (Property<String>) hc.getContainerProperty(idItem, "caption");
			captionProperty.setValue((String) rootElt[0]);
			@SuppressWarnings("unchecked")
			Property<UploadedPmsi.Status> statusProperty = (Property<UploadedPmsi.Status>) hc.getContainerProperty(idItem, "status");
			statusProperty.setValue((UploadedPmsi.Status) rootElt[1]);
			@SuppressWarnings("unchecked")
			Property<Integer> depthProperty = (Property<Integer>) hc.getContainerProperty(idItem, "depth");
			depthProperty.setValue(0);
			
		}
		
		// TREE WIDGET
		finessTree = new Tree();
		finessTree.setContainerDataSource(hc);
		finessTree.setItemCaptionPropertyId("caption");
		finessTree.setImmediate(true);
		
		// ADDS THE LISTENERS
		ExpandListener el = new ExpandListener(hc, this);
		CollapseListener cl = new CollapseListener(hc);
		ItemClickListener icl = new ItemClickListener(rootElement, hc);

		finessTree.addExpandListener(el);
		finessTree.addCollapseListener(cl);
		finessTree.addItemClickListener(icl);

		// ADD THE ACTION HANDLERS
		finessTree.addActionHandler(new DeleteHandler(hc, this));
		
		// SETS THE CONTENT OF THIS PANEL
		setContent(finessTree);

	}
	
	public void removeItem(Object itemId) {
		// CHECKS THE DEPTH
		Integer eventDepth =
				(Integer) hc.getContainerProperty(
						itemId, "depth").getValue();
		Object parentId = hc.getParent(itemId);

		hc.removeItemRecursively(itemId);
		// CHECK IF PARENT HAS NO CHILDREN AFTER ITEM REMOVING AND IF DEPTH IS SUPERIOR TO 1
		if (parentId != null && eventDepth > 1) {
			@SuppressWarnings("unchecked")
			Collection<Object> children = (Collection<Object>) hc.getChildren(parentId);
			if (children == null || children.size() == 0) {
				removeItem(parentId);
			}
		}
	}
}
