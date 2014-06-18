package com.github.aiderpmsi.pimsdriver.db.vaadin.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

/**
 * Serves as a decoupling tool between vaadin and datasources
 * @author jpc
 *
 */
public class DBFilterMapper {

	private HashMap<Object, Object> map;
	
	public DBFilterMapper(HashMap<Object, Object> map) {
		this.map = map;
	}
	
	public List<Filter> mapFilters(List<Filter> filters) {
		// MAPS THE NAME OF THE VAADIN FILTERS TO SQL FILTER
		List<Filter> sqlFilters = new ArrayList<>(filters.size());
		for (Filter filter : filters) {
			sqlFilters.add(map(filter));
		}
		return sqlFilters;
	}

	private Filter map(Filter filter) {
		if (filter instanceof And) {
			// OLD FILTER
			And andFilter = (And) filter;
			// NEW LIST OF AND FILTERS
			List<Filter> newFilters = new ArrayList<>(andFilter.getFilters().size());
			// POPULATE NEW FILTERS
			for (Filter oldFilter : andFilter.getFilters()) {
				newFilters.add(this.map(oldFilter));
			}
			// NEW FILTER
			And newFilter = new And(newFilters.toArray(new Filter[0]));
			return newFilter;
		} else if (filter instanceof Between) {
			// OLD FILTER
			Between betweenFilter = (Between) filter;
			// NEW FILTER
			Between newFilter = new Between(
					map.get(betweenFilter.getPropertyId()),
					betweenFilter.getStartValue(),
					betweenFilter.getEndValue());
			return newFilter;
		} else if (filter instanceof Compare.Equal) {
			// OLD FILTER
			Compare.Equal compareFilter = (Compare.Equal) filter;
			// NEW FILTER
			Compare.Equal newFilter = new Compare.Equal(
					map.get(compareFilter.getPropertyId()),
					compareFilter.getValue());
			return newFilter;
		} else if (filter instanceof Compare.Greater) {
			// OLD FILTER
			Compare.Greater compareFilter = (Compare.Greater) filter;
			// NEW FILTER
			Compare.Greater newFilter = new Compare.Greater(
					map.get(compareFilter.getPropertyId()),
					compareFilter.getValue());
			return newFilter;
		} else if (filter instanceof Compare.GreaterOrEqual) {
			// OLD FILTER
			Compare.GreaterOrEqual compareFilter = (Compare.GreaterOrEqual) filter;
			// NEW FILTER
			Compare.GreaterOrEqual newFilter = new Compare.GreaterOrEqual(
					map.get(compareFilter.getPropertyId()),
					compareFilter.getValue());
			return newFilter;
		} else if (filter instanceof Compare.Less) {
			// OLD FILTER
			Compare.Less compareFilter = (Compare.Less) filter;
			// NEW FILTER
			Compare.Less newFilter = new Compare.Less(
					map.get(compareFilter.getPropertyId()),
					compareFilter.getValue());
			return newFilter;
		} else if (filter instanceof Compare.LessOrEqual) {
			// OLD FILTER
			Compare.LessOrEqual compareFilter = (Compare.LessOrEqual) filter;
			// NEW FILTER
			Compare.LessOrEqual newFilter = new Compare.LessOrEqual(
					map.get(compareFilter.getPropertyId()),
					compareFilter.getValue());
			return newFilter;
		} else if (filter instanceof IsNull) {
			// OLD FILTER
			IsNull isnullFilter = (IsNull) filter;
			// NEW FILTER
			IsNull newFilter = new IsNull(
					map.get(isnullFilter.getPropertyId()));
			return newFilter;
		} else if (filter instanceof Like) {
			// OLD FILTER
			Like likeFilter = (Like) filter;
			// NEW FILTER
			Like newFilter = new Like(
					(String) map.get(likeFilter.getPropertyId()),
					likeFilter.getValue(),
					likeFilter.isCaseSensitive());
			return newFilter;
		} else if (filter instanceof Not) {
			// OLD FILTER
			Not notFilter = (Not) filter;
			// NEW FILTER
			Not newFilter = new Not(
					this.map(notFilter.getFilter()));
			return newFilter;
		} else if (filter instanceof Or) {
			// OLD FILTER
			Or orFilter = (Or) filter;
			// NEW LIST OF AND FILTERS
			List<Filter> newFilters = new ArrayList<>(orFilter.getFilters());
			// POPULATE NEW FILTERS
			for (Filter oldFilter : orFilter.getFilters()) {
				newFilters.add(this.map(oldFilter));
			}
			// NEW FILTER
			Or newFilter = new Or(newFilters.toArray(new Filter[0]));
			return newFilter;
		} else if (filter instanceof SimpleStringFilter) {
			// OLD FILTER
			SimpleStringFilter ssFilter = (SimpleStringFilter) filter;
			// NEW FILTER
			SimpleStringFilter newFilter = new SimpleStringFilter(
					map.get(ssFilter.getPropertyId()),
					ssFilter.getFilterString(),
					ssFilter.isIgnoreCase(),
					ssFilter.isOnlyMatchPrefix());
			return newFilter;
		} else {
			return null;
		}
	}

	public List<OrderBy> mapOrderBys(List<Entry<Object, Boolean>> vaadinOrderBys) {
		List<OrderBy> orderBys = new ArrayList<>(vaadinOrderBys.size());
		for (Entry<Object, Boolean> vaadinOrderBy : vaadinOrderBys) {
			Object mappedTo;
			if ((mappedTo = map.get(vaadinOrderBy.a)) == null) {
				throw new IllegalArgumentException("Entry " + vaadinOrderBy.a.toString() + " unknown");
			} else {
				orderBys.add(new OrderBy(mappedTo.toString(), vaadinOrderBy.b));
			}
		}
		return orderBys;
	}

}
