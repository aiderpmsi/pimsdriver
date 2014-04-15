package com.github.aiderpmsi.pimsdriver.vaadin;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi.UploadedElement;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.github.aiderpmsi.pimsdriver.odb.vaadin.ODBQueryBuilder;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;

public class PmsiProcessQuery implements Query {

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
		return new BeanItem<UploadedElement>(new UploadedElement());
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int startIndex, int count) {
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(contentQuery + " OFFSET " + startIndex + " LIMIT " + count);
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(contentQueryArgs);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// CREATE THE LIST OF ITEMS
		List<Item> items = new ArrayList<>(count);

		// Fills the list of items
		for (ODocument result : results) {
			// BEAN FOR THIS ITEM
			UploadedElement element = new UploadedElement();

			// FILLS THE BEAN
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi((Date) result.field("dateenvoi"));
			element.setFiness((String) result.field("finess"));
			element.setMonth((Integer) result.field("month"));
			element.setProcessed((String) result.field("processed"));
			element.setYear((Integer) result.field("year"));
			element.setSuccess((Boolean) result.field("success"));
			element.setErrorComment((String) result.field("errorComment"));
			if (result.field("rss") == null)
				element.setComment("RSF seul");
			else
				element.setComment("RSF et RSS");

			// CREATES THE ITEM FROM THE BEAN
			BeanItem<UploadedElement> ueItem = new BeanItem<UploadedElement>(element);
			
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
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(countQuery);
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(contentQueryArgs);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// GETS THE FIRST RESULT
		return ((Long) results.get(0).field("nbrows")).intValue();
	}

}
