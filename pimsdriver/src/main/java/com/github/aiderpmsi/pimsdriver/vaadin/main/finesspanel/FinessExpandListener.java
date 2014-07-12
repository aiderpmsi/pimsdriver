package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO.YM;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel.FinessComponent.FinessContainerModel;
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

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
	
	private final HierarchicalContainer hc;
	
	private final FinessComponent fp;
	
	private final ServletContext context;
	
	public FinessExpandListener(final HierarchicalContainer hc, final FinessComponent fp, final ServletContext context) {
		this.hc = hc;
		this.fp = fp;
		this.context = context;
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
		ActionEncloser.executeVoid( (exception) -> "Impossible de charger l'arbre des téléversements",
				() -> {
				switch (depth) {
				case 0:
					createFinessNodes(hc, event.getItemId(), status); break;
				case 1:
					createYMNodes(hc, event.getItemId(), status, finess); break;
				case 2:
					createUploadsNodes(hc, event.getItemId(), status, finess, year, month); break;
				default:
					throw new ActionException();
				}});
	}
	
	private void createFinessNodes(final HierarchicalContainer hc, final Object itemId, final UploadedPmsi.Status status) throws ActionException {
		final FinessContainerModel containerModel = new FinessContainerModel(null, null, new Integer(1), null, null, status, null);
		for (final String finess : new NavigationActions(context).getDistinctFinesses(status)) {
			// CREATES THE NODE
			containerModel.setCaption(finess);
			containerModel.setFiness(finess);
			final Object newItemId = fp.createContainerItemNode(hc, containerModel);
			// ATTACHES THE NODE TO ITS PARENT
			hc.setParent(newItemId, itemId);
		}
	}
	
	private void createYMNodes(HierarchicalContainer hc, Object itemId, UploadedPmsi.Status status, String finess) throws ActionException {
		final FinessContainerModel containerModel = new FinessContainerModel(null, finess, new Integer(2), null, null, status, null);

		final List<YM> yms = new NavigationActions(context).getYM(status, finess);
		
		if (yms.size() == 0) {
			// IF THERE IS NO YM, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT
			fp.removeContainerItem(hc, itemId);
			// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
			Notification.show("Le finess sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
		} else {
			// SOME UPLOADS EXIST, CREATE THE CORRESPONDING NODES
			for (final YM ym : yms) {
				// SETS THE ENTRY ELEMENTS
				containerModel.setCaption(ym.year + " M" + ym.month);
				containerModel.setYear(ym.year);
				containerModel.setMonth(ym.month);
				// CREATES THE NODE
				final Object newItemId = fp.createContainerItemNode(hc, containerModel);
				// ATTACHES THE NODE
				hc.setParent(newItemId, itemId);
			}
		}
	}

	public void createUploadsNodes(HierarchicalContainer hc, Object itemId, UploadedPmsi.Status status, String finess, Integer year, Integer month) throws ActionException {
		final FinessContainerModel containerModel = new FinessContainerModel(null, finess, new Integer(3), year, month, status, null);

		// CREATE THE QUERY FILTER
		final List<Filter> filters = new ArrayList<>(1);
		filters.add(new And(
				new Compare.Equal("plud_finess", finess),
				new Compare.Equal("plud_processed", status),
				new Compare.Equal("plud_year", year),
				new Compare.Equal("plud_month", month)));
		
		// CREATE THE QUERY ORDER BY
		final List<OrderBy> orderBys = new ArrayList<>(1);
		orderBys.add(new OrderBy("plud_dateenvoi", false));

		// LOAD THE ITEMS
		final List<UploadedPmsi> ups = new NavigationActions(context).getUploadedPmsi(filters, orderBys, null, null);

		// IF WE HAVE NO RESULT, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
		if (ups.size() == 0) {
			fp.removeContainerItem(hc, itemId);
			// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
			Notification.show("L'élément sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
		} else {
			// WE HAVE TO ADD THESE ITEMS TO THE TREE
			for (final UploadedPmsi model : ups) {
				// SETS THE ENTRY ELEMENTS
				containerModel.setCaption(sdf.format(model.dateenvoi));
				containerModel.setModel(model);
				// CREATES THE NODE
				final Object newItemId = fp.createContainerItemNode(hc, containerModel);
				// ATTACHES THE NODE
				hc.setParent(newItemId, itemId);
				hc.setChildrenAllowed(newItemId, false);
			}
		}
	}
}