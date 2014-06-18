package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryMapping;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery.BaseQueryInit;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class RsfBDetailsQueryFactory implements QueryFactory {
	
	private Object[][] mappings = new Object[][] {
			{"pmel_id", "pmel_id"},
			{"pmel_root", "pmel_root"},
			{"pmel_parent", "pmel_parent"},
			{"pmel_position", "pmel_position"},
			{"pmel_line", "pmel_line"},
			{"formatteddatedebutsejour", "cast_to_date(datedebutsejour, NULL)"},
			{"formatteddatefinsejour", "cast_to_date(datefinsejour, NULL)"},
			{"codeacte", "codeacte"},
			{"quantite", "quantite"},
			{"numghs", "numghs"},
			{"formattedmontanttotaldepense", "cast_to_int(montanttotaldepense, NULL)"}
	};

	private BaseQueryInit<BaseRsfB> bqi;
	
	private DBQueryMapping mapping;
	
	public RsfBDetailsQueryFactory(final Long pmel_root, final Long pmel_position) {
		// CREATES THE QUERY INITIALIZER
		bqi = new BaseQueryInit<BaseRsfB>() {

			@Override
			public void initFilters(List<Filter> filters) {
				filters.add(new Compare.Equal("pmel_root", pmel_root));
				filters.add(new Compare.Equal("pmel_parent", pmel_position));
			}

			@Override
			public void initOrders(LinkedList<Entry<Object, Boolean>> orderbys) {
				if (orderbys.size() == 0) {
					Entry<Object, Boolean> entry = new Entry<>();
					entry.a = "pmel_position"; entry.b = true;
					orderbys.add(entry);
				}
			}

			@Override
			public BaseRsfB constructBean() {
				return new BaseRsfB();
			}

			@Override
			public List<BaseRsfB> loadBeans(List<Filter> filters,
					List<OrderBy> orderBys, int startIndex, int count)
					throws ActionException {
					return new NavigationActions().getFacturesB(filters, orderBys, startIndex, count);
			}

			@Override
			public String loadBeansError(Exception e) {
				return "Erreur de lecture de la liste des factures B";
			}

			@Override
			public int size(List<Filter> Filters) throws ActionException {
				return new NavigationActions().getFacturesBSize(Filters);
			}

			@Override
			public String sizeError(Exception e) {
				return "Erreur de lecture de la liste des factures B";
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
