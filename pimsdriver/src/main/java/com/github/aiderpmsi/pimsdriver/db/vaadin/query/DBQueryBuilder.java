package com.github.aiderpmsi.pimsdriver.db.vaadin.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.AndTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.BetweenTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.CompareTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.DBTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.IsNullTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.LikeTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.NotTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.OrTranslator;
import com.github.aiderpmsi.pimsdriver.db.vaadin.translators.SimpleStringTranslator;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class DBQueryBuilder {

	private static ArrayList<DBTranslator> filterTranslators = new ArrayList<>(8);

    static {
    	/* Register all default filter translators */
    	addFilterTranslator(new AndTranslator());
    	addFilterTranslator(new OrTranslator());
    	addFilterTranslator(new LikeTranslator());
    	addFilterTranslator(new BetweenTranslator());
    	addFilterTranslator(new CompareTranslator());
    	addFilterTranslator(new NotTranslator());
    	addFilterTranslator(new IsNullTranslator());
    	addFilterTranslator(new SimpleStringTranslator());
    }

    public synchronized static void addFilterTranslator(
    		DBTranslator translator) {
        filterTranslators.add(translator);
    }

    /**
     * Constructs and returns a string representing the filter that can be used
     * in a WHERE clause.
     * 
     * @param filter
     *            the filter to translate
     * @return a string representing the filter.
     */
    public synchronized static String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        for (DBTranslator ft : filterTranslators) {
            if (ft.translatesFilter(filter)) {
                return ft.getWhereStringForFilter(filter, arguments);
            }
        }
        return "";
    }

    public static String getJoinedFilterString(Collection<Filter> filters,
            String joinString, List<Object> arguments) {
        StringBuilder result = new StringBuilder();
        
        for (Filter f : filters) {
            result.append(getWhereStringForFilter(f, arguments));
            result.append(" ").append(joinString).append(" ");
        }

        // Remove the last instance of joinString
        result.delete(result.length() - joinString.length() - 2,
                result.length());

        return result.toString();
    }

    public static String getWhereStringForFilters(List<Filter> filters, List<Object> arguments) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(getJoinedFilterString(filters, "AND", arguments));
        return where.toString();
    }

    public static String getOrderStringForOrderBys(List<OrderBy> orders, List<Object> arguments) {
    	if (orders == null || orders.isEmpty()) {
    		return "";
    	}
    	StringBuilder order = new StringBuilder(" ORDER BY ");
    	for (OrderBy orderby : orders) {
    		order.append(orderby.getColumn()).append(" ").
    			append(orderby.isAscending() ? "ASC" : "DESC").append(", ");
    	}
    	// REMOVE REMAINING COMMA
    	order.delete(order.length() - 2, order.length());
    	return order.toString();
    }

}