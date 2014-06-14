package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.DBFilterMapper;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO.YM;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.UploadPmsiMapping;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class ExpandListener implements Tree.ExpandListener {

	private static final long serialVersionUID = 8913677773696542760L;

	private HierarchicalContainer hc;
	
	private FinessPanel fp;
	
	@SuppressWarnings("unused")
	private ExpandListener() {}
	
	public ExpandListener(HierarchicalContainer hc, FinessPanel fp) {
		this.hc = hc;
		this.fp = fp;
	}
	
	private Object createNode(Object[][] elements) {
		Object id = hc.addItem();
		for (Object[] element : elements) {
			@SuppressWarnings("unchecked")
			Property<Object> prop = (Property<Object>) hc.getContainerProperty(id, element[0]);
			prop.setValue(element[1]);
		}
		return id;
	}
	
	@Override
	public synchronized void nodeExpand(ExpandEvent event) {

		// GETS THE EVENT NODE DEPTH
		Integer eventDepth =
				(Integer) hc.getContainerProperty(
						event.getItemId(), "depth").getValue();
		// GETS THE STATUS OF THE NODE (SUCCESSED OR FAILED)
		UploadedPmsi.Status eventStatus =
				(UploadedPmsi.Status) hc.getContainerProperty(
						event.getItemId(), "status").getValue();
		
		// NAVIGATION ACTIONS
		NavigationActions na = new NavigationActions();
		
		// IF WE EXPAND A ROOT NODE
		if (eventDepth == 0) {
			// FILL THE CORRESPONDING DISTINCT FINESSES
			try {
				List<String> finesses = na.getDistinctFinesses(eventStatus);
				for (String finess : finesses) {
					// CREATES THE NODE
					Object id = createNode(new Object[][] {
							new Object[] {"caption", finess},
							new Object[] {"finess", finess},
							new Object[] {"depth", new Integer(1)},
							new Object[] {"status", eventStatus}
					});
					// ATTACHES THE NODE
					hc.setParent(id, event.getItemId());
				}
			} catch (ActionException e) {
				Notification.show("Erreur de chargement des différents finess", Notification.Type.WARNING_MESSAGE);
			}
		}

		// IF WE EXPAND A FINESS NODE
		else if (eventDepth == 1) {
			// GETS THE FINESS
			String finess = (String) hc.getContainerProperty(event.getItemId(), "finess").getValue();

			try {
				// FILL THE CORRESPONDING DISTINCT YEAR / MONTHS FOR THIS FINESS
				List<YM> yms = na.getYM(eventStatus, finess);

				// WHEN NO YMS EXIST, THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
				if (yms.size() == 0) {
					fp.removeItem(event.getItemId());
					// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
					Notification.show("Le finess sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
				}
				// THIS EXISTS, ADD THE ELEMENTS IN THE TREE
				else {
					for (YM ym : yms) {
						// CREATES THE NODE
						Object id = createNode(new Object[][] {
								new Object[] {"caption", ym.year + " M" + ym.month},
								new Object[] {"year", ym.year},
								new Object[] {"month", ym.month},
								new Object[] {"finess", finess},
								new Object[] {"status", eventStatus},
								new Object[] {"depth", new Integer(2)}
						});
						// ATTACHES THE NODE
						hc.setParent(id, event.getItemId());
					}
				}
			} catch (ActionException e) {
				Notification.show("Impossible de sélectionner les dates pmsi téléversées", Notification.Type.WARNING_MESSAGE);
			}
		}
		
		// IF WE EXPAND A YEAR / MONTH NODE
		else if (eventDepth == 2) {
			String finess = (String) hc.getContainerProperty(event.getItemId(), "finess").getValue();
			Integer year = (Integer) hc.getContainerProperty(event.getItemId(), "year").getValue();
			Integer month = (Integer) hc.getContainerProperty(event.getItemId(), "month").getValue();

			// CREATE THE QUERY'S FILTER AND ORDER
			List<Filter> filters = new ArrayList<>(1);
			filters.add(new And(
					new Compare.Equal("finess", finess),
					new Compare.Equal("processed", eventStatus),
					new Compare.Equal("year", year),
					new Compare.Equal("month", month)
			));
			List<OrderBy> orders = new ArrayList<>();
			orders.add(new OrderBy("dateenvoi", false));
			
			// TRANSLATE THE FILTERS AND ORDERS
			DBFilterMapper fm = new DBFilterMapper(UploadPmsiMapping.sqlMapping);
			List<Filter> sqlFilters = fm.mapFilters(filters);
			List<OrderBy> sqlOrderBys = fm.mapOrderBys(orders);
			
			// LOAD THE ITEMS
			try {
				List<UploadedPmsi> ups = na.getUploadedPmsi(sqlFilters, sqlOrderBys, null, null);
			
				// IF WE HAVE NO RESULT, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
				if (ups.size() == 0) {
					fp.removeItem(event.getItemId());
					// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
					Notification.show("L'élément sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
				}
				// WE HAVE TO ADD THESE ITEMS TO THE TREE
				else {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
					for (UploadedPmsi model : ups) {
						// CREATES THE NODE
						Object id = createNode(new Object[][] {
								new Object[] {"caption", sdf.format(model.dateenvoi)},
								new Object[] {"model", model},
								new Object[] {"year", year},
								new Object[] {"month", month},
								new Object[] {"finess", finess},
								new Object[] {"status", eventStatus},
								new Object[] {"depth", new Integer(3)}
						});
						// ATTACHES THE NODE
						hc.setParent(id, event.getItemId());
						hc.setChildrenAllowed(id, false);
					}
				}
			} catch (ActionException e) {
				Notification.show("Impossible de sélectionner les éléments téléversés", Notification.Type.WARNING_MESSAGE);
			}
		}
	}
}
