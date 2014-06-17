package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.DBFilterMapper;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.ui.Notification;

public class RsfBDetailsQuery implements Query{

	List<Filter> sqlFilters;
	List<OrderBy> sqlOrderBys;
	
	protected RsfBDetailsQuery() {}
	
	public RsfBDetailsQuery(long pmel_root, long pmel_position, QueryDefinition qd) {
		
		// MAPS THE NAME OF THE VAADIN FILTERS TO SQL FILTER
		DBFilterMapper fm = new DBFilterMapper(RsfBDetailsQueryMapping.sqlMapping);
		
		// MAP FILTERS
		qd.getFilters().add(new Compare.Equal("pmel_root", pmel_root));
		qd.getFilters().add(new Compare.Equal("pmel_parent", pmel_position));
		sqlFilters = fm.mapFilters(qd.getFilters());
		
		// CREATES THE ORDERS
		sqlOrderBys = new ArrayList<>(qd.getSortPropertyIds().length + 1);
		sqlOrderBys.add(new OrderBy((String) RsfBDetailsQueryMapping.sqlMapping.get("pmel_position"), true));
		for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
			sqlOrderBys.add(new OrderBy(
					(String) RsfBDetailsQueryMapping.sqlMapping.get((String) qd.getSortPropertyIds()[i]),
					qd.getSortPropertyAscendingStates().length < i ? true : qd.getSortPropertyAscendingStates()[i]));
		}	
		
	}
	
	@Override
	public Item constructItem() {
		return new BeanItem<BaseRsfB>(new BaseRsfB());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int startIndex, int count) {
		// CREATES LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);

		NavigationActions na = new NavigationActions();
		
		try {
			// GETS THE LIST OF UPLOADED ELEMENTS
			List<BaseRsfB> elements = na.getFacturesB(sqlFilters, sqlOrderBys, startIndex, count);

			// FILLS LIST OF ITEMS
			for (BaseRsfB element : elements) {
				items.add(new BeanItem<BaseRsfB>(element));
			}

		} catch (ActionException e) {
			Notification.show("Erreur de lecture de la liste des factures", Notification.Type.WARNING_MESSAGE);
		}

		return items;
	}

	@Override
	public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		// SIZE
		Integer size = 0;

		NavigationActions na = new NavigationActions();
		
		try {
			// GETS THE LIST OF UPLOADED ELEMENTS
			size = na.getFacturesBSize(sqlFilters);
		} catch (ActionException e) {
			Notification.show("Erreur de lecture de la liste de fichiers", Notification.Type.WARNING_MESSAGE);
		}

		return size;
	}

}
