package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Like;

@SuppressWarnings("serial")
public class LikeTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Like;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        Like like = (Like) filter;
        if (like.isCaseSensitive()) {
            arguments.add(like.getValue());
            return (String)like.getPropertyId() + " LIKE ?";
        } else {
            arguments.add(like.getValue().toUpperCase());
            return "UPPER(" + (String)like.getPropertyId() + ") LIKE ?";
        }
    }
}
