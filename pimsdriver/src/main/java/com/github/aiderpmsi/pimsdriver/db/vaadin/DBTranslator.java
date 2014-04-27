package com.github.aiderpmsi.pimsdriver.db.vaadin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.vaadin.data.Container.Filter;

public interface DBTranslator extends Serializable {

	public boolean translatesFilter(Filter filter);

    public String getWhereStringForFilter(Filter filter, HashMap<String, String> tableFieldsMapping, List<Object> arguments);
}
