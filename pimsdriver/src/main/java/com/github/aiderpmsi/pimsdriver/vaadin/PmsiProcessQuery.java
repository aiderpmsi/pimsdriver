package com.github.aiderpmsi.pimsdriver.vaadin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDAO;
import com.github.aiderpmsi.pimsdriver.model.UploadedElementModel;
import com.github.aiderpmsi.pimsdriver.odb.vaadin.ODBQueryBuilder;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;

public class PmsiProcessQuery implements Query{

	/** Query used for the count */
	private String countQuery;
	/** Query used for the content */
	private String contentQuery;
	/** Arguments for the contentQuery */
	private Object[] contentQueryArgs;
	
	protected PmsiProcessQuery() {}
	
	public PmsiProcessQuery(QueryDefinition qd) {
		
		StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(*) as nbrows FROM PmsiUpload ");
		StringBuilder contentQueryBuilder = new StringBuilder("SELECT * FROM PmsiUpload ");
		
		// ADDS THE FILTERS
		List<Filter> filters = new LinkedList<>(qd.getFilters());
		Compare filter = new Compare.Equal("processed", "pending");
		filters.add(new And(filter));
		List<Object> contentQueryArgsList = new LinkedList<>();
		// CREATES THE FILTERS AND FILLS THE ARGUMENTS
		String filtersQuery = ODBQueryBuilder.getWhereStringForFilters(filters, contentQueryArgsList);
		contentQueryArgs = contentQueryArgsList.toArray();
		
		// ADDS THE ORDERINGS
		StringBuilder orderBuilder = new StringBuilder();
		if (qd.getSortPropertyIds().length != 0) {
			orderBuilder.append(" ORDER BY ");
			for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
				orderBuilder.append((String) qd.getSortPropertyIds()[i]);
				if ((qd.getSortPropertyAscendingStates().length < i) || 
						qd.getSortPropertyAscendingStates()[i]) {
					orderBuilder.append(" ASC ");
				} else {
					orderBuilder.append(" DESC ");
				}
			}
		}
		
		// MERGES COUNT AND CONTENTBUILDER
		countQueryBuilder.append(filtersQuery);
		contentQueryBuilder.append(filtersQuery).append(orderBuilder);
		
		countQuery = countQueryBuilder.toString();
		contentQuery = contentQueryBuilder.toString();
	}
	
	@Override
	public Item constructItem() {
		return new BeanItem<UploadedElementModel>(new UploadedElementModel());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int startIndex, int count) {
		// GETS THE LIST OF UPLOADED ELEMENTS
		UploadedElementsDAO ued = new UploadedElementsDAO();
		List<UploadedElementModel> elements = ued.getUploadedElements(contentQuery + " OFFSET " + startIndex + " LIMIT " + count, contentQueryArgs);
		
		// CREATE THE LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);
		for (UploadedElementModel element : elements) {
			// CREATES THE ITEM FROM THE BEAN
			BeanItem<UploadedElementModel> ueItem = new BeanItem<UploadedElementModel>(element);
			
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
		UploadedElementsDAO ued = new UploadedElementsDAO();
		return ued.size(countQuery, new Object[]{"pending"});
	}

}
