package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;


import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;


public class FacturesQueryFactory implements QueryFactory {

	long recordid;
	
	public FacturesQueryFactory(long recordid) {
		this.recordid = recordid;
	}
	
	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new FacturesQuery(recordid, qd);
	}

}
