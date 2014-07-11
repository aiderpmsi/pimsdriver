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
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssMain;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class SejoursQueryFactory implements QueryFactory {
	
	private Object[][] mappings = new Object[][] {
			{"pmel_id", "smva.pmel_id"},
			{"pmel_root", "smva.pmel_root"},
			{"pmel_position", "smva.pmel_position"},
			{"pmel_line", "smva.pmel_line"},
			{"numrss", "trim(smva.numrss)"},
			{"ghm", "trim(smva.numcmd) || trim(smva.numghm)"},
			{"ghmcorrige", "pmgr.pmgr_racine || pmgr.pmgr_modalite || pmgr.pmgr_gravite || pmgr.pmgr_erreur"},
			{"numlocalsejour", "trim(smva.numlocalsejour)"},
			{"numrum", "trim(smva.numrum)"},
			{"numunitemedicale", "trim(smva.numunitemedicale)"},
			{"formatteddateentree", "cast_to_date(smva.dateentree, NULL)"},
			{"formatteddatesortie", "cast_to_date(smva.datesortie, NULL)"},
			{"nbseances", "cast_to_int(smva.nbseances, NULL)"},
			{"dp", "trim(smva.dp)"},
			{"dr", "trim(smva.dr)"}
	};

	private final SejoursQueryInit sejoursQueryInit;
	
	private final DBQueryMapping mapping;
	
	public SejoursQueryFactory(final Long pmel_root, final ServletContext context) {
		// CREATES THE QUERY INITIALIZER
		sejoursQueryInit = new SejoursQueryInit(pmel_root, context);
		// CREATES THE MAPPING
		mapping = new DBQueryMapping(mappings);
	}
	
	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new BaseQuery<>(sejoursQueryInit, mapping, qd);
	}

	public class SejoursQueryInit implements BaseQueryInit<BaseRssMain> {

		private final Long pmel_root;
		
		private final ServletContext context;
		
		public SejoursQueryInit(final Long pmel_root, final ServletContext context) {
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
		public BaseRssMain constructBean() {
			return new BaseRssMain();
		}

		@Override
		public List<BaseRssMain> loadBeans(List<Filter> filters,
				List<OrderBy> orderBys, int startIndex, int count)
				throws ActionException {
				return new NavigationActions(context).getRssMainList(filters, orderBys, startIndex, count);
		}

		@Override
		public String loadBeansError(Exception e) {
			return "Erreur de lecture de la liste des séjours";
		}

		@Override
		public int size(List<Filter> Filters) throws ActionException {
			return new NavigationActions(context).getRssMainSize(Filters);
		}

		@Override
		public String sizeError(Exception e) {
			return "Erreur de lecture de la liste des séjours";
		}
	}
	
}
