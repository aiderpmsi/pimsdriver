package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.IsNull;

@SuppressWarnings("serial")
public class IsNullTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof IsNull;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        IsNull in = (IsNull) filter;
        return (String) in.getPropertyId() + " IS NULL";
	}

}
