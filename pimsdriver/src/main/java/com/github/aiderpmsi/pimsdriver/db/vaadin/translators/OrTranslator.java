package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryBuilder;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Or;

@SuppressWarnings("serial")
public class OrTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Or;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        return DBQueryBuilder.getJoinedFilterString(
                ((Or) filter).getFilters(), "OR", arguments);
	}

}
