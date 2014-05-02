package com.github.aiderpmsi.pimsdriver.vaadin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.dao.UploadPmsiDTOB;
import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.db.vaadin.DBQueryBuilder;
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
	
	private final static HashMap<String, String> tableFieldsMapping = new HashMap<>();
	
	{
		tableFieldsMapping.put("id", "plud_id");
		tableFieldsMapping.put("processed", "plud_processed");
		tableFieldsMapping.put("finess", "plud_finess");
		tableFieldsMapping.put("year", "plud_year");
		tableFieldsMapping.put("month", "plud_month");
		tableFieldsMapping.put("dateenvoi", "plud_dateenvoi");
	}
	
	protected PmsiProcessQuery() {}
	
	public PmsiProcessQuery(QueryDefinition qd) {
		
		StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(*) as nbrows FROM plud_pmsiupload ");
		StringBuilder contentQueryBuilder = new StringBuilder(
				"SELECT plud_id, plud_processed, plud_finess, plud_year, plud_month, plud_dateenvoi "
				+ "FROM plud_pmsiupload ");
		
		// ADDS THE FILTERS
		List<Filter> filters = new LinkedList<>(qd.getFilters());
		Compare filter = new Compare.Equal("processed", UploadedPmsi.Status.pending);
		filters.add(new And(filter));
		List<Object> contentQueryArgsList = new LinkedList<>();
		// CREATES THE FILTERS AND FILLS THE ARGUMENTS
		String filtersQuery = DBQueryBuilder.getWhereStringForFilters(filters, tableFieldsMapping, contentQueryArgsList);
		contentQueryArgs = contentQueryArgsList.toArray();
		
		// ADDS THE ORDERINGS
		StringBuilder orderBuilder = new StringBuilder();
		if (qd.getSortPropertyIds().length != 0) {
			orderBuilder.append(" ORDER BY ");
			for (int i = 0 ; i < qd.getSortPropertyIds().length ; i++) {
				orderBuilder.append(tableFieldsMapping.get((String) qd.getSortPropertyIds()[i]));
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
		return new BeanItem<UploadedPmsi>(new UploadedPmsi());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int startIndex, int count) {
		// GETS THE LIST OF UPLOADED ELEMENTS
		UploadPmsiDTOB ued = new UploadPmsiDTOB();
		List<UploadedPmsi> elements = ued.getUploadedElements(contentQuery + " OFFSET " + startIndex + " LIMIT " + count, contentQueryArgs);
		
		// CREATE THE LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);
		for (UploadedPmsi element : elements) {
			// CREATES THE ITEM FROM THE BEAN
			BeanItem<UploadedPmsi> ueItem = new BeanItem<UploadedPmsi>(element);
			
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
		UploadPmsiDTOB ued = new UploadPmsiDTOB();
		return (int) ued.size(countQuery, contentQueryArgs);
	}

}
