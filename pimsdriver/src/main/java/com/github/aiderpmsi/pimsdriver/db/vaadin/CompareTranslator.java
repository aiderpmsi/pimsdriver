package com.github.aiderpmsi.pimsdriver.db.vaadin;

import java.util.List;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;

@SuppressWarnings("serial")
public class CompareTranslator implements DBTranslator {

	@Override
	public boolean translatesFilter(Filter filter) {
		return filter instanceof Compare;
	}

	@Override
	public String getWhereStringForFilter(Filter filter, List<Object> arguments) {
        Compare compare = (Compare) filter;

        // ADAPTS THE COMPARE PROPERTY TYPE
        Object compareValue = compare.getValue(); 
        String postpand = "";
        if (compareValue instanceof PmsiUploadedElementModel.Status) {
        	postpand = "::plud_status";
        }
        
        // ADDS THE VALUE OF THE COMPARE
        arguments.add(compare.getValue());
        
        String prop = (String) compare.getPropertyId();
        switch (compare.getOperation()) {
        case EQUAL:
            return prop + " = ?" + postpand;
        case GREATER:
            return prop + " > ?" + postpand;
        case GREATER_OR_EQUAL:
            return prop + " >= ?" + postpand;
        case LESS:
            return prop + " < ?" + postpand;
        case LESS_OR_EQUAL:
            return prop + " <= ?" + postpand;
        default:
            return "";
        }
	}

}
