package com.github.aiderpmsi.pimsdriver.vaadin.finesspanel;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.ExpandEvent;

public class FinessPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 5192397393504372354L;

	public FinessPanel() {
		
		// SETS THE HIERARCHICAL CONTAINER PROPERTIES
		final HierarchicalContainer hc = new HierarchicalContainer();
		hc.addContainerProperty("caption", String.class, "");
		hc.addContainerProperty("depth", Integer.class, null);
		hc.addContainerProperty("year", Integer.class, null);
		hc.addContainerProperty("month", Integer.class, null);
		hc.addContainerProperty("recordid", Long.class, null);
		
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
			public synchronized void nodeExpand(ExpandEvent event) {
				// GETS THE EVENT NODE DEPTH
				Integer eventDepth = (Integer) hc.getContainerProperty(event.getItemId(), "depth").getValue();
				// IF WE EXPAND A ROOT NODE
				if (eventDepth == 0) {
					PmsiUploadedElementModel.Status filter =
							(event.getItemId() == idsuccess ? PmsiUploadedElementModel.Status.successed : PmsiUploadedElementModel.Status.failed);
					// FILL THE SUCCESS TREE
					List<String> finesses = (new NavigationDTO()).getFiness(filter);
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
				else if (eventDepth == 1) {
					String filter = (hc.getParent(event.getItemId()) == idsuccess ? "processed" : "failed");
					String finess = (String) hc.getContainerProperty(event.getItemId(), "caption").getValue();
					// FILL THE FINESS TREE :
					List<NavigationDTO.YM> yms = (new NavigationDTO()).getYM(filter, finess);
					// WHEN YMS IS NULL, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
					if (yms == null) {
						hc.removeItemRecursively(event.getItemId());
						// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
						Notification.show("Le finess sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
					} else {
						for (NavigationDTO.YM ym : yms) {
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
				// IF WE EXPAND A YEAR / MONTH NODE
				else if (eventDepth == 2) {
					String filter = (hc.getParent(hc.getParent(event.getItemId())) == idsuccess ? "processed" : "failed");
					String finess = (String) hc.getContainerProperty(hc.getParent(event.getItemId()), "caption").getValue();
					Integer year = (Integer) hc.getContainerProperty(event.getItemId(), "year").getValue();
					Integer month = (Integer) hc.getContainerProperty(event.getItemId(), "month").getValue();
					Object[] arguments = new Object[] {filter, finess, year, month};
					UploadedElementsDTO ued = new UploadedElementsDTO();
					// FILLS THE DATEENVOI TREE
					List<PmsiUploadedElementModel> models = 
							ued.getUploadedElements(
									"SELECT * FROM PmsiUpload WHERE processed = ? AND finess = ? AND year = ? AND month = ? ORDER BY dateenvoi DESC",
									arguments);
					// IF WE HAVE NO RESULT, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
					if (models.size() == 0) {
						hc.removeItemRecursively(event.getItemId());
					} else {
						for (PmsiUploadedElementModel model : models) {
							Object id = hc.addItem();
							@SuppressWarnings("unchecked")
							Property<String> prop = (Property<String>) hc.getContainerProperty(id, "caption");
							SimpleDateFormat sdf = new SimpleDateFormat("DD/mm/YYYY HH:MM:SS");
							prop.setValue(sdf.format(model.getDateenvoi()));
							@SuppressWarnings("unchecked")
							Property<Long> proprid = (Property<Long>) hc.getContainerProperty(id, "RID");
							proprid.setValue(model.getRecordId());
							@SuppressWarnings("unchecked")
							Property<Integer> depth = (Property<Integer>) hc.getContainerProperty(id, "depth");
							depth.setValue(3);
							hc.setParent(id, event.getItemId());
						}
					}
				}
			}
		});
		
		finessTree.addCollapseListener(new Tree.CollapseListener() {
			/** Generated serial id */
			private static final long serialVersionUID = -8083420762047096032L;
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
		});
	}
	
}
