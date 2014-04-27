package com.github.aiderpmsi.pimsdriver.vaadin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDTO;
import com.github.aiderpmsi.pimsdriver.db.vaadin.DBQueryBuilder;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.data.util.sqlcontainer.query.generator.StatementHelper;

public class PmsiProcessQuery implements Query{

	/** Order by clause */
	private List<OrderBy> obs;
	
	/** Filters */
	private List<Filter> filters;

	protected PmsiProcessQuery() {}
	
	public PmsiProcessQuery(QueryDefinition qd) {
		
		// GENERATES THE ASKED ORDERING
		obs = new ArrayList<>(qd.getDefaultSortPropertyIds().length);
		for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
			OrderBy ob = new OrderBy((String) qd.getSortPropertyIds()[i],
					qd.getSortPropertyAscendingStates().length < i ? true : qd.getSortPropertyAscendingStates()[i]);
			obs.add(ob);
		}
	}
	
	@Override
	public Item constructItem() {
		return new BeanItem<PmsiUploadedElementModel>(new PmsiUploadedElementModel());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int startIndex, int count) {
		// GETS THE LIST OF UPLOADED ELEMENTS
		// CREATES THE FILTERS AND FILLS THE ARGUMENTS
		DefaultSQLGenerator dsg = new DefaultSQLGenerator(StatementHelper.class);
		StatementHelper sh = dsg.generateSelectQuery("pmsielement", filters, obs, startIndex, count, "*");
		sh.
	
	StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(*) as nbrows FROM PmsiUpload ");
		StringBuilder contentQueryBuilder = new StringBuilder("SELECT * FROM PmsiUpload ");
		
		// CREATES THE FILTERS AND FILLS THE ARGUMENTS
		DefaultSQLGenerator dsg = new DefaultSQLGenerator(StatementHelper.class);
		String filtersQuery = dsg.generateSelectQuery("pmsielement", qd.getFilters(), qd.get, offset, pagelength, toSelect)("public.pmsielement", , orderBys, offset, pagelength, toSelect)getWhereStringForFilters(filters, contentQueryArgsList);
		contentQueryArgs = contentQueryArgsList.toArray();
		
		
		// MERGES COUNT AND CONTENTBUILDER
		countQueryBuilder.append(filtersQuery);
		contentQueryBuilder.append(filtersQuery).append(orderBuilder);
		
		countQuery = countQueryBuilder.toString();
		contentQuery = contentQueryBuilder.toString();

		UploadedElementsDTO ued = new UploadedElementsDTO();
		List<PmsiUploadedElementModel> elements = ued.getUploadedElements(contentQuery + " OFFSET " + startIndex + " LIMIT " + count, contentQueryArgs);
		
		// CREATE THE LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);
		for (PmsiUploadedElementModel element : elements) {
			// CREATES THE ITEM FROM THE BEAN
			BeanItem<PmsiUploadedElementModel> ueItem = new BeanItem<PmsiUploadedElementModel>(element);
			
			// ADDS IT TO THE LIST
			items.add(ueItem);
		}

		return items;

	}

	@Override
	public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
		 throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		UploadedElementsDTO ued = new UploadedElementsDTO();
		return ued.size(countQuery, new Object[]{"pending"});
	}

}
