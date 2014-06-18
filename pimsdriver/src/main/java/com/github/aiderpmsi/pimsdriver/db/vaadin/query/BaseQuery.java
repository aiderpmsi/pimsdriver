package com.github.aiderpmsi.pimsdriver.db.vaadin.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class BaseQuery<R> implements Query {

	public interface BaseQueryInit<R> {
		public void initFilters(List<Filter> filters);
		public void initOrders(LinkedList<Entry<Object, Boolean>> orderbys);
		public R constructBean();
		public List<R> loadBeans(List<Filter> filters, List<OrderBy> orderBys, int startIndex, int count) throws ActionException;
		public String loadBeansError(Exception e);
		public int size(List<Filter> Filters) throws ActionException;
		public String sizeError(Exception e);
	}
	
	private List<Filter> sqlFilters;
	
	private List<OrderBy> sqlOrderBys;
		
	private BaseQueryInit<R> bqi;
	
	public BaseQuery(BaseQueryInit<R> queryInit, DBQueryMapping mapping, QueryDefinition qd) {
		
		this.bqi = queryInit;
		
		// MAPS THE NAME OF THE VAADIN FILTERS TO SQL FILTER
		DBFilterMapper fm = new DBFilterMapper(mapping);

		// INIT FILTERS
		queryInit.initFilters(qd.getFilters());
		// MAP FILTERS
		sqlFilters = fm.mapFilters(qd.getFilters());
		
		// CREATES THE ORDERS LIST
		LinkedList<Entry<Object, Boolean>> vaadinOrderBys = new LinkedList<>();
		for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
			Entry<Object, Boolean> entry = new Entry<>();
			entry.a = qd.getSortPropertyIds()[i];
			entry.b = qd.getDefaultSortPropertyAscendingStates().length < i ? true :
				qd.getDefaultSortPropertyAscendingStates()[i];
			vaadinOrderBys.add(entry);
		}
		// INIT ORDERS LIST
		queryInit.initOrders(vaadinOrderBys);
		// MAP ORDER BYS
		sqlOrderBys = fm.mapOrderBys(vaadinOrderBys);

	}
	
	@Override
	public Item constructItem() {
		return new BeanItem<R>(bqi.constructBean());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(final int startIndex, final int count) {
		// CREATES LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);
		
		List<R> beans = ActionEncloser.executer(new ActionEncloser.ActionReturner<List<R>>() {
			@Override
			public List<R> action() throws ActionException {
				return bqi.loadBeans(sqlFilters, sqlOrderBys, startIndex, count);
			}
			@Override
			public String msgError(ActionException e) {
				return bqi.loadBeansError(e);
			}
		});
		
		if (beans != null) {
			// FILS THE LIST OF ITEMS FROM THE BEANS
			for (R bean : beans) {
				items.add(new BeanItem<R>(bean));
			}
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
		Integer size = ActionEncloser.executer(new ActionEncloser.ActionReturner<Integer>() {
			@Override
			public Integer action() throws ActionException {
				return bqi.size(sqlFilters);
			}
			@Override
			public String msgError(ActionException e) {
				return bqi.sizeError(e);
			}
		});

		if (size == null)
			size = 0;
		
		return size;
	}

}
