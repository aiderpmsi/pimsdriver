package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.io.Serializable;
import java.util.List;

import com.vaadin.data.Container.Filter;

public interface DBTranslator extends Serializable {

	public boolean translatesFilter(Filter filter);

    public String getWhereStringForFilter(Filter filter, List<Object> arguments);
}
