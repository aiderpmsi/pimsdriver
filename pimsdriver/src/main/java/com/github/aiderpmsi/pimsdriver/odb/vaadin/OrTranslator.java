package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Or;

@SuppressWarnings("serial")
public class OrTranslator implements ODBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Or;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        return ODBQueryBuilder.getJoinedFilterString(
                ((Or) filter).getFilters(), "OR", arguments);
	}

}
