package com.github.aiderpmsi.pimsdriver.db.vaadin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container.Filter;

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

}
