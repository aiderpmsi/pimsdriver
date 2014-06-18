package com.github.aiderpmsi.pimsdriver.db.vaadin.translators;

import java.util.List;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
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
		// USED TO CONSTRUCT EXPRESSION
		String prefix, postfix;
		
		Compare compare = (Compare) filter;

        // ADAPTS THE COMPARE PROPERTY TYPE
        Object compareValue = compare.getValue(); 
        if (compareValue instanceof UploadedPmsi.Status) {
        	UploadedPmsi.Status status =
        			(UploadedPmsi.Status) compareValue;
        	postfix = "::plud_status";
        	arguments.add(status.toString());
        } else {
        	postfix = "";
        	arguments.add(compareValue);
        }
        
        prefix = (String) compare.getPropertyId();

        switch (compare.getOperation()) {
        case EQUAL:
            return prefix + " = ?" + postfix;
        case GREATER:
            return prefix + " > ?" + postfix;
        case GREATER_OR_EQUAL:
            return prefix + " >= ?" + postfix;
        case LESS:
            return prefix + " < ?" + postfix;
        case LESS_OR_EQUAL:
            return prefix + " <= ?" + postfix;
        default:
            return "";
        }
	}

}
