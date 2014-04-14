package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;

@SuppressWarnings("serial")
public class CompareTranslator implements ODBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Compare;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        Compare compare = (Compare) filter;
        arguments.add(compare.getValue());
        String prop = (String) compare.getPropertyId();
        switch (compare.getOperation()) {
        case EQUAL:
            return prop + " = ?";
        case GREATER:
            return prop + " > ?";
        case GREATER_OR_EQUAL:
            return prop + " >= ?";
        case LESS:
            return prop + " < ?";
        case LESS_OR_EQUAL:
            return prop + " <= ?";
        default:
            return "";
        }
	}

}
