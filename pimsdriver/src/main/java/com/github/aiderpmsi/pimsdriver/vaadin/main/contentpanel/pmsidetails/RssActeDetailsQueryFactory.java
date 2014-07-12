package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsidetails;

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
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssActe;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class RssActeDetailsQueryFactory implements QueryFactory {
	
	private final Object[][] mappings = new Object[][] {
			{"pmel_id", "pmel_id"},
			{"pmel_root", "pmel_root"},
			{"pmel_parent", "pmel_parent"},
			{"pmel_position", "pmel_position"},
			{"pmel_line", "pmel_line"},
			{"formatteddaterealisation", "cast_to_date(daterealisation, NULL)"},
			{"codeccam", "trim(codeccam)"},
			{"phase", "trim(phase)"},
			{"activite", "trim(activite)"},
			{"nbacte", "cast_to_int(nbacte, NULL)"}
	};

	private final BaseQueryInit<BaseRssActe> bqi;
	
	private final DBQueryMapping mapping;
	
	public RssActeDetailsQueryFactory(final Long pmel_root, final Long pmel_position, final ServletContext context) {
		// CREATES THE QUERY INITIALIZER
		bqi = new BaseQueryInit<BaseRssActe>() {

			@Override
			public void initFilters(final List<Filter> filters) {
				filters.add(new Compare.Equal("pmel_root", pmel_root));
				filters.add(new Compare.Equal("pmel_parent", pmel_position));
			}

			@Override
			public void initOrders(final LinkedList<Entry<Object, Boolean>> orderbys) {
				if (orderbys.size() == 0) {
					final Entry<Object, Boolean> entry = new Entry<>((Object)"pmel_position", true);
					orderbys.add(entry);
				}
			}

			@Override
			public BaseRssActe constructBean() {
				return new BaseRssActe();
			}

			@Override
			public List<BaseRssActe> loadBeans(final List<Filter> filters,
					final List<OrderBy> orderBys, final int startIndex, final int count)
					throws ActionException {
					return new NavigationActions(context).getRssActeList(filters, orderBys, startIndex, count);
			}

			@Override
			public String loadBeansError(final Exception e) {
				return "Erreur de lecture de la liste des actes";
			}

			@Override
			public int size(final List<Filter> Filters) throws ActionException {
				return new NavigationActions(context).getRssActeSize(Filters);
			}

			@Override
			public String sizeError(final Exception e) {
				return "Erreur de lecture de la liste des actes";
			}
		};
		
		// CREATES THE MAPPING
		mapping = new DBQueryMapping(mappings);
	}
	
	@Override
	public Query constructQuery(final QueryDefinition qd) {
		return new BaseQuery<>(bqi, mapping, qd);
	}

}
