package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Like;

@SuppressWarnings("serial")
public class LikeTranslator implements ODBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Like;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        Like like = (Like) filter;
        if (like.isCaseSensitive()) {
            arguments.add(like.getValue());
            return like.getPropertyId() + " LIKE ?";
        } else {
            arguments.add(like.getValue().toUpperCase());
            return like.getPropertyId() + ".toUpperCase() LIKE ?";
        }
    }
}
