package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
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
		new Object[] {"Finess", PmsiUploadedElementModel.Status.successed},
		new Object[] {"Fichiers en erreur", PmsiUploadedElementModel.Status.failed}
	};
	
	@SuppressWarnings("unused")
	private FinessPanel() {};
	
	public FinessPanel(RootWindow rootElement) {
	
		// SETS THE NAVIGATION DTO (REUSABLE)
		NavigationDTO navigationDTO = new NavigationDTO();
		
		// SETS THE HIERARCHICAL CONTAINER PROPERTIES
		final HierarchicalContainer hc = new HierarchicalContainer();
		hc.addContainerProperty("caption", String.class, "");
		hc.addContainerProperty("finess", String.class, null);
		hc.addContainerProperty("depth", Integer.class, null);
		hc.addContainerProperty("year", Integer.class, null);
		hc.addContainerProperty("month", Integer.class, null);
		hc.addContainerProperty("status", PmsiUploadedElementModel.Status.class, null);
		hc.addContainerProperty("model", PmsiUploadedElementModel.class, null);
		
		// FILLS THE ROOT ELEMENTS FROM ELEMENTS LIST
		for (Object[] rootElt : rootElts) {
			Object idItem = hc.addItem();
			@SuppressWarnings("unchecked")
			Property<String> captionProperty = (Property<String>) hc.getContainerProperty(idItem, "caption");
			captionProperty.setValue((String) rootElt[0]);
			@SuppressWarnings("unchecked")
			Property<PmsiUploadedElementModel.Status> statusProperty = (Property<PmsiUploadedElementModel.Status>) hc.getContainerProperty(idItem, "status");
			statusProperty.setValue((PmsiUploadedElementModel.Status) rootElt[1]);
			@SuppressWarnings("unchecked")
			Property<Integer> depthProperty = (Property<Integer>) hc.getContainerProperty(idItem, "depth");
			depthProperty.setValue(0);
		}
		
		// TREE WIDGET
		Tree finessTree = new Tree();
		finessTree.setContainerDataSource(hc);
		finessTree.setItemCaptionPropertyId("caption");

		// ADDS THE LISTENERS
		ExpandListener el = new ExpandListener(hc, navigationDTO);
		CollapseListener cl = new CollapseListener(hc);
		ItemClickListener icl = new ItemClickListener(rootElement, hc);

		finessTree.addExpandListener(el);
		finessTree.addCollapseListener(cl);
		finessTree.addItemClickListener(icl);
		
		// SETS THE CONTENT OF THIS PANEL
		setContent(finessTree);

	}
	
}
