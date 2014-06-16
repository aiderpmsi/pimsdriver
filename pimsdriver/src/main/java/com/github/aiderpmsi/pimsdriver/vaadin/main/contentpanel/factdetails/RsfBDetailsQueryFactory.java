package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;


import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;


public class RsfBDetailsQueryFactory implements QueryFactory {

	private long pmel_root, pmel_position;
	
	public RsfBDetailsQueryFactory(long pmel_root, long pmel_position) {
		this.pmel_root = pmel_root;
		this.pmel_position = pmel_position;
	}
	
	@Override
	public Query constructQuery(QueryDefinition qd) {
		return new RsfBDetailsQuery(pmel_root, pmel_position, qd);
	}

}
