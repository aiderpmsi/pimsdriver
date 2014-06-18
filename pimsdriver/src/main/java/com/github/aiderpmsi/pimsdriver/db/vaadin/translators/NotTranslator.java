package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryBuilder;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Not;

@SuppressWarnings("serial")
public class NotTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Not;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
		Not not = (Not) filter;
        if (not.getFilter() instanceof IsNull) {
            IsNull in = (IsNull) not.getFilter();
            return (String) in.getPropertyId() + " IS NOT NULL";
        }
        return " !("
                + DBQueryBuilder.getWhereStringForFilter(not.getFilter(), arguments) + ") ";
	}

}
