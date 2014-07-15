package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;
import com.github.aiderpmsi.pimsdriver.vaadin.main.SplitPanel;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree;

public class FinessComponent extends Tree {

	/** Generated serial id */
	private static final long serialVersionUID = 5192397393504372354L;
	
	private final SplitPanel splitPanel;
	
	@SuppressWarnings("serial")
	private final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer() {{
		// SETS THE ELEMENTS IN THE CONTAINER
		addContainerProperty("caption", String.class, "");
		addContainerProperty("finess", String.class, null);
		addContainerProperty("depth", Integer.class, null);
		addContainerProperty("year", Integer.class, null);
		addContainerProperty("month", Integer.class, null);
		addContainerProperty("status", UploadedPmsi.Status.class, null);
		addContainerProperty("model", UploadedPmsi.class, null);
		// FILLS THE ROOT
		for (UploadedPmsi.Status status : UploadedPmsi.Status.values()) {
			if (status != UploadedPmsi.Status.pending) {
				FinessContainerModel fcm = new FinessContainerModel(status.getLabel(), null, 0, null, null, status, null);
				createContainerItemNode(this, fcm);
			}
		}
	}};
		
	public FinessComponent(SplitPanel splitPanel) {
		super();
		this.splitPanel = splitPanel;

		// SETS THE CONTAINER ORIGIN
		setContainerDataSource(hierarchicalContainer);
		setItemCaptionPropertyId("caption"); // PROPERTY ID USED AS CAPTION FOR EACH ELEMENT IN TREE 
		setImmediate(true); // MODIFICATIONS SHOULD BE SHOWED IMMEDIATELY
		
		// ADDS THE LISTENERS
		ExpandListener el = new FinessExpandListener(hierarchicalContainer, this, getSplitPanel().getRootWindow().getMainApplication().getServletContext());
		CollapseListener cl = new FinessCollapseListener(hierarchicalContainer);
		ItemClickListener icl = new ItemClickListener(getSplitPanel().getRootWindow(), hierarchicalContainer);

		addExpandListener(el);
		addCollapseListener(cl);
		addItemClickListener(icl);

		// ADD THE ACTION HANDLERS
		addActionHandler(new DeleteHandler(hierarchicalContainer, this, getSplitPanel().getRootWindow().getMainApplication().getServletContext()));
	}

	@SuppressWarnings("unchecked")
	public final Object createContainerItemNode(HierarchicalContainer hc, FinessContainerModel fcm) {
		
		final Object itemId = hc.addItem();
		
		for (Method getter : fcm.getClass().getDeclaredMethods()) {
			final String getterName = getter.getName();
			if (getter.getParameterCount() == 0 && getterName.length() > 3 && getterName.startsWith("get")) {
				// WE ARE IN AN GETTER, RETRIEVE THE NAME PROPERTY
				final StringBuilder propertyNameBuilder = new StringBuilder(getterName.substring(3, 4).toLowerCase());
				propertyNameBuilder.append(getterName.substring(4));
				final String propertyName = propertyNameBuilder.toString(); 
				// SETS THE CORRESPONDING PROPERTY
				try {
					hc.getContainerProperty(itemId, propertyName).setValue(getter.invoke(fcm));
				} catch (IllegalAccessException | ReadOnlyException | IllegalArgumentException | InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		return itemId;
	}

	public SplitPanel getSplitPanel() {
		return splitPanel;
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

	public static class FinessContainerModel {
		private String caption = "";
		private String finess = null;
		private Integer depth = null;
		private Integer year = null;
		private Integer month = null;
		private UploadedPmsi.Status status = null;
		private UploadedPmsi model = null;
		public FinessContainerModel(String caption, String finess,
				Integer depth, Integer year, Integer month, Status status,
				UploadedPmsi model) {
			this.caption = caption;
			this.finess = finess;
			this.depth = depth;
			this.year = year;
			this.month = month;
			this.status = status;
			this.model = model;
		}
		public String getCaption() {
			return caption;
		}
		public void setCaption(final String caption) {
			this.caption = caption;
		}
		public String getFiness() {
			return finess;
		}
		public void setFiness(final String finess) {
			this.finess = finess;
		}
		public Integer getDepth() {
			return depth;
		}
		public void setDepth(final Integer depth) {
			this.depth = depth;
		}
		public Integer getYear() {
			return year;
		}
		public void setYear(final Integer year) {
			this.year = year;
		}
		public Integer getMonth() {
			return month;
		}
		public void setMonth(final Integer month) {
			this.month = month;
		}
		public UploadedPmsi.Status getStatus() {
			return status;
		}
		public void setStatus(final UploadedPmsi.Status status) {
			this.status = status;
		}
		public UploadedPmsi getModel() {
			return model;
		}
		public void setModel(final UploadedPmsi model) {
			this.model = model;
		}
		
	}

}
