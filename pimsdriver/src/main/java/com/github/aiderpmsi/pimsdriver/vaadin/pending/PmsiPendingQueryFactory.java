package com.github.aiderpmsi.pimsdriver.vaadin.pending;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery.BaseQueryInit;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryMapping;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class PmsiPendingQueryFactory implements QueryFactory {

	private Object[][] mappings = new Object[][] {
			{"finess", "plud_finess"},
			{"year", "plud_year"},
			{"month", "plud_month"},
			{"dateenvoi", "plud_dateenvoi"},
			{"processed", "plud_processed"}
	};

	private BaseQueryInit<UploadedPmsi> bqi;
	
	private DBQueryMapping mapping;
	
	public PmsiPendingQueryFactory() {
		// CREATES THE QUERY INITIALIZER
		bqi = new BaseQueryInit<UploadedPmsi>() {

			@Override
			public void initFilters(List<Filter> filters) {
				filters.add(new Compare.Equal("processed", UploadedPmsi.Status.pending));
			}

			@Override
			public void initOrders(LinkedList<Entry<Object, Boolean>> orderbys) {
				// DO NOTHING
			}

			@Override
			public UploadedPmsi constructBean() {
				return new UploadedPmsi();
			}

			@Override
			public List<UploadedPmsi> loadBeans(List<Filter> filters,
					List<OrderBy> orderBys, int startIndex, int count)
					throws ActionException {
					return new NavigationActions().getUploadedPmsi(filters, orderBys, startIndex, count);
			}

			@Override
			public String loadBeansError(Exception e) {
				return "Erreur de lecture de la liste de fichiers";
			}

			@Override
			public int size(List<Filter> Filters) throws ActionException {
				return new NavigationActions().getUploadedPmsiSize(Filters);
			}

			@Override
			public String sizeError(Exception e) {
				return "Erreur de lecture de la liste de fichiers";
			}
		};
		
		// CREATES THE MAPPING
		mapping = new DBQueryMapping(mappings);
	}
	
	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new BaseQuery<>(bqi, mapping, qd);
	}

}
