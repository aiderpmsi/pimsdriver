package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;

@SuppressWarnings("serial")
public class AndTranslator implements ODBTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof And;
    }

    public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
    	return ODBQueryBuilder.getJoinedFilterString(
    			((And) filter).getFilters(), "AND", arguments);
    }

}