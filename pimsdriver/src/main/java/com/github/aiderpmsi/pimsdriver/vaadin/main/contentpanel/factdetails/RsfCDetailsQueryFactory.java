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
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class RsfCDetailsQueryFactory implements QueryFactory {
	
	private Object[][] mappings = new Object[][] {
			{"pmel_id", "pmel_id"},
			{"pmel_root", "pmel_root"},
			{"pmel_parent", "pmel_parent"},
			{"pmel_position", "pmel_position"},
			{"pmel_line", "pmel_line"},
			{"formatteddateacte", "cast_to_date(dateacte, NULL)"},
			{"codeacte", "codeacte"},
			{"quantite", "quantite"},
			{"formattedmontanttotalhonoraire", "cast_to_int(montanttotalhonoraire, NULL)"}
	};

	private BaseQueryInit<BaseRsfC> bqi;
	
	private DBQueryMapping mapping;
	
	public RsfCDetailsQueryFactory(final Long pmel_root, final Long pmel_position) {
		// CREATES THE QUERY INITIALIZER
		bqi = new BaseQueryInit<BaseRsfC>() {

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
			public BaseRsfC constructBean() {
				return new BaseRsfC();
			}

			@Override
			public List<BaseRsfC> loadBeans(List<Filter> filters,
					List<OrderBy> orderBys, int startIndex, int count)
					throws ActionException {
					return new NavigationActions().getFacturesC(filters, orderBys, startIndex, count);
			}

			@Override
			public String loadBeansError(Exception e) {
				return "Erreur de lecture de la liste des factures";
			}

			@Override
			public int size(List<Filter> Filters) throws ActionException {
				return new NavigationActions().getFacturesCSize(Filters);
			}

			@Override
			public String sizeError(Exception e) {
				return "Erreur de lecture de la liste des factures";
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
