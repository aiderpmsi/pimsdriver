package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.SimpleStringFilter;

@SuppressWarnings("serial")
public class SimpleStringTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof SimpleStringFilter;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
		SimpleStringFilter ssf = (SimpleStringFilter) filter;

		String likeStr =
				(ssf.isOnlyMatchPrefix() ? ssf.getFilterString() + "%"
	                : "%" + ssf.getFilterString() + "%");
		Like like = new Like(ssf.getPropertyId().toString(), likeStr);
		like.setCaseSensitive(!ssf.isIgnoreCase());
		return new LikeTranslator().getWhereStringForFilter(like, arguments);

	}

}
