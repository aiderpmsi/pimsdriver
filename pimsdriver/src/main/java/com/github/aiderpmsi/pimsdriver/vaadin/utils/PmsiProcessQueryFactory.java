package com.github.aiderpmsi.pimsdriver.vaadin.utils;


import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;


public class PmsiProcessQueryFactory implements QueryFactory {

	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new PmsiProcessQuery(qd);
	}

}
