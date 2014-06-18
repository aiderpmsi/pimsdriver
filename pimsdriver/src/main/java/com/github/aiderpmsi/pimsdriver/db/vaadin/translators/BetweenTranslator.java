package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Between;

@SuppressWarnings("serial")
public class BetweenTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Between;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        Between between = (Between) filter;
        arguments.add(between.getStartValue());
        arguments.add(between.getEndValue());
        return (String) between.getPropertyId() + " BETWEEN ? AND ?";
	}

}
