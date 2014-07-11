package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.BaseQuery.BaseQueryInit;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryMapping;
import com.github.aiderpmsi.pimsdriver.db.vaadin.query.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class FacturesQueryFactory implements QueryFactory {
	
	private final Object[][] mappings = new Object[][] {
			{"pmel_id", "pmel_id"},
			{"pmel_root", "pmel_root"},
			{"pmel_position", "pmel_position"},
			{"pmel_line", "pmel_line"},
			{"numfacture", "trim(numfacture)"},
			{"numrss", "trim(numrss)"},
			{"codess", "trim(codess)"},
			{"sexe", "trim(sexe)"},
			{"formatteddatenaissance", "cast_to_date(datenaissance, NULL)"},
			{"formatteddateentree", "cast_to_date(dateentree, NULL)"},
			{"formatteddatesortie", "cast_to_date(datesortie, NULL)"},
			{"formattedtotalfacturehonoraire", "cast_to_int(totalfacturehonoraire, NULL)"},
			{"formattedtotalfactureph", "cast_to_int(totalfactureph, NULL)"},
			{"etatliquidation", "etatliquidation"}
	};

	private final FacturesQueryInit facturesQueryInit;
	
	private final DBQueryMapping mapping;
	
	public FacturesQueryFactory(final Long pmel_root, final ServletContext context) {
		// CREATES THE QUERY INITIALIZER
		facturesQueryInit = new FacturesQueryInit(pmel_root, context);
		// CREATES THE MAPPING
		mapping = new DBQueryMapping(mappings);
	}
	
	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new BaseQuery<>(facturesQueryInit, mapping, qd);
	}

	public class FacturesQueryInit implements BaseQueryInit<BaseRsfA> {

		private final Long pmel_root;
		
		private final ServletContext context;
		
		public FacturesQueryInit(final Long pmel_root, final ServletContext context) {
			this.pmel_root = pmel_root;
			this.context = context;
		}
		
		@Override
		public void initFilters(List<Filter> filters) {
			filters.add(new Compare.Equal("pmel_root", pmel_root));
		}

		@Override
		public void initOrders(LinkedList<Entry<Object, Boolean>> orderbys) {
			if (orderbys.size() == 0) {
				Entry<Object, Boolean> entry = new Entry<>((Object)"pmel_position", true);
				orderbys.add(entry);
			}
		}

		@Override
		public BaseRsfA constructBean() {
			return new BaseRsfA();
		}

		@Override
		public List<BaseRsfA> loadBeans(List<Filter> filters,
				List<OrderBy> orderBys, int startIndex, int count)
				throws ActionException {
				return new NavigationActions(context).getFactures(filters, orderBys, startIndex, count);
		}

		@Override
		public String loadBeansError(Exception e) {
			return "Erreur de lecture de la liste des factures";
		}

		@Override
		public int size(List<Filter> Filters) throws ActionException {
			return new NavigationActions(context).getFacturesSize(Filters);
		}

		@Override
		public String sizeError(Exception e) {
			return "Erreur de lecture de la liste des factures";
		}
	};
}
