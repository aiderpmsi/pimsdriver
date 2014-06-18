package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.Entry;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO.YM;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class FinessExpandListener implements Tree.ExpandListener {

	private static final long serialVersionUID = 8913677773696542760L;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
	
	private HierarchicalContainer hc;
	
	private FinessComponent fp;
	
	@SuppressWarnings("unused")
	private FinessExpandListener() {}
	
	public FinessExpandListener(HierarchicalContainer hc, FinessComponent fp) {
		this.hc = hc;
		this.fp = fp;
	}
	
	@Override
	public synchronized void nodeExpand(final ExpandEvent event) {

		// GETS CONTENT OF THIS ITEM
		final Integer depth = (Integer) hc.getContainerProperty(event.getItemId(), "depth").getValue();
		final UploadedPmsi.Status status = (UploadedPmsi.Status) hc.getContainerProperty(event.getItemId(), "status").getValue();
		final String finess = (String) hc.getContainerProperty(event.getItemId(), "finess").getValue();
		final Integer year = (Integer) hc.getContainerProperty(event.getItemId(), "year").getValue();
		final Integer month = (Integer) hc.getContainerProperty(event.getItemId(), "month").getValue();
		
		// IF WE EXPAND A ROOT NODE
		ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
				switch (depth) {
				case 0:
					createFinessNodes(hc, event.getItemId(), status);
					break;
				case 1:
					createYMNodes(hc, event.getItemId(), status, finess);
					break;
				case 2:
					createUploadsNodes(hc, event.getItemId(), status, finess, year, month);
					break;
				default:
					throw new ActionException();
				}
			}
			@Override
			public String msgError(ActionException e) {
				return "Impossible de charger l'arbre des téléversements";
			}
		});
	}
	
	private void createFinessNodes(HierarchicalContainer hc, Object itemId, UploadedPmsi.Status status) throws ActionException {
		@SuppressWarnings("unchecked")
		Entry<Object, Object> entries[] = new Entry[4];
		entries[0] = new Entry<Object, Object>("depth", new Integer(1));
		entries[1] = new Entry<Object, Object>("status", status);
		entries[2] = new Entry<Object, Object>("caption", null);
		entries[3] = new Entry<Object, Object>("finess", null);
		for (String finess : new NavigationActions().getDistinctFinesses(status)) {
			// CREATES THE NODE
			entries[2].b = finess;
			entries[3].b = finess;
			Object newItemId = fp.createContainerItemNode(hc, entries);
			// ATTACHES THE NODE TO ITS PARENT
			hc.setParent(newItemId, itemId);
		}
	}
	
	private void createYMNodes(HierarchicalContainer hc, Object itemId, UploadedPmsi.Status status, String finess) throws ActionException {
		@SuppressWarnings("unchecked")
		Entry<Object, Object> entries[] = new Entry[6];
		entries[0] = new Entry<Object, Object>("depth", new Integer(2));
		entries[1] = new Entry<Object, Object>("status", status);
		entries[2] = new Entry<Object, Object>("finess", finess);
		entries[3] = new Entry<Object, Object>("caption", null);
		entries[4] = new Entry<Object, Object>("year", null);
		entries[5] = new Entry<Object, Object>("month", null);
		
		List<YM> yms = new NavigationActions().getYM(status, finess);
		
		if (yms.size() == 0) {
			// IF THERE IS NO YM, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT
			fp.removeContainerItem(hc, itemId);
			// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
			Notification.show("Le finess sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
		} else {
			// SOME UPLOADS EXIST, CREATE THE CORRESPONDING NODES
			for (YM ym : yms) {
				// SETS THE ENTRY ELEMENTS
				entries[3].b = ym.year + " M" + ym.month;
				entries[4].b = ym.year;
				entries[5].b = ym.month;
				// CREATES THE NODE
				Object newItemId = fp.createContainerItemNode(hc, entries);
				// ATTACHES THE NODE
				hc.setParent(newItemId, itemId);
			}
		}
	}

	public void createUploadsNodes(HierarchicalContainer hc, Object itemId, UploadedPmsi.Status status, String finess, Integer year, Integer month) throws ActionException {
		@SuppressWarnings("unchecked")
		Entry<Object, Object> entries[] = new Entry[7];
		entries[0] = new Entry<Object, Object>("depth", new Integer(3));
		entries[1] = new Entry<Object, Object>("status", status);
		entries[2] = new Entry<Object, Object>("finess", finess);
		entries[3] = new Entry<Object, Object>("year", year);
		entries[4] = new Entry<Object, Object>("month", month);
		entries[5] = new Entry<Object, Object>("caption", null);
		entries[6] = new Entry<Object, Object>("model", null);

		// CREATE THE QUERY FILTER
		List<Filter> filters = new ArrayList<>(1);
		filters.add(new And(
				new Compare.Equal("plud_finess", finess),
				new Compare.Equal("plud_processed", status),
				new Compare.Equal("plud_year", year),
				new Compare.Equal("plud_month", month)));
		
		// CREATE THE QUERY ORDER BY
		List<OrderBy> orderBys = new ArrayList<>(1);
		orderBys.add(new OrderBy("plud_dateenvoi", true));

		// LOAD THE ITEMS
		List<UploadedPmsi> ups = new NavigationActions().getUploadedPmsi(filters, orderBys, null, null);

		// IF WE HAVE NO RESULT, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
		if (ups.size() == 0) {
			fp.removeContainerItem(hc, itemId);
			// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
			Notification.show("L'élément sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
		} else {
			// WE HAVE TO ADD THESE ITEMS TO THE TREE
			for (UploadedPmsi model : ups) {
				// SETS THE ENTRY ELEMENTS
				entries[5].b = sdf.format(model.dateenvoi);
				entries[6].b = model;
				// CREATES THE NODE
				Object newItemId = fp.createContainerItemNode(hc, entries);
				// ATTACHES THE NODE
				hc.setParent(newItemId, itemId);
				hc.setChildrenAllowed(newItemId, false);
			}
		}
	}
}
