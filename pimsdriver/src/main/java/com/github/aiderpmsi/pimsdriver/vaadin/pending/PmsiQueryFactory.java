package com.github.aiderpmsi.pimsdriver.vaadin.pending;


import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;


public class PmsiQueryFactory implements org.vaadin.addons.lazyquerycontainer.QueryFactory {

	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new PmsiProcessQuery(qd);
	}

}
