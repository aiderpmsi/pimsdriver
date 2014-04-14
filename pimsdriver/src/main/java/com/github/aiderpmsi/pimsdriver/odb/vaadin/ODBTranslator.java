package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.io.Serializable;
import java.util.List;

import com.vaadin.data.Container.Filter;

public interface ODBTranslator extends Serializable {

	public boolean translatesFilter(Filter filter);

    public String getWhereStringForFilter(Filter filter, List<Object> arguments);
}
