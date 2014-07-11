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
	
	private final List<Filter> sqlFilters;
	
	private final List<OrderBy> sqlOrderBys;
		
	private final BaseQueryInit<R> bqi;
	
	public BaseQuery(final BaseQueryInit<R> queryInit, final DBQueryMapping mapping, final QueryDefinition qd) {
		
		this.bqi = queryInit;
		
		// MAPS THE NAME OF THE VAADIN FILTERS TO SQL FILTER
		final DBFilterMapper fm = new DBFilterMapper(mapping);

		// INIT FILTERS
		queryInit.initFilters(qd.getFilters());
		// MAP FILTERS
		sqlFilters = fm.mapFilters(qd.getFilters());
		
		// CREATES THE ORDERS LIST
		final LinkedList<Entry<Object, Boolean>> vaadinOrderBys = new LinkedList<>();
		for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
			final Entry<Object, Boolean> entry = new Entry<>(
					qd.getSortPropertyIds()[i],
					qd.getSortPropertyAscendingStates().length < i ? true :
						qd.getSortPropertyAscendingStates()[i]);
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
		final List<Item> items = new ArrayList<>(count);
		
		final List<R> beans = ActionEncloser.execute( (exception) -> bqi.loadBeansError(exception),
				() -> bqi.loadBeans(sqlFilters, sqlOrderBys, startIndex, count));

		if (beans != null) {
			// FILS THE LIST OF ITEMS FROM THE BEANS
			for (final R bean : beans) {
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
		final Integer size = ActionEncloser.execute( (exception) -> bqi.sizeError(exception),
				() -> bqi.size(sqlFilters));

		if (size == null)
			return 0;
		else
			return size;
	}

}
