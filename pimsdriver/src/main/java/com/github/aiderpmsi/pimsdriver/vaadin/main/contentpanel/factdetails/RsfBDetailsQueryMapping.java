package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.HashMap;

public class RsfBDetailsQueryMapping {

	public static final HashMap<Object, Object> sqlMapping = new HashMap<>();
	
	static {
		sqlMapping.put("pmel_id", "pmel_id");
		sqlMapping.put("pmel_line", "pmel_line");
		sqlMapping.put("datedebutsejour", "datedebutsejour");
		sqlMapping.put("datefinsejour", "datefinsejour");
		sqlMapping.put("codeacte", "codeacte");
		sqlMapping.put("quantite", "quantite");
		sqlMapping.put("numghs", "numghs");
		sqlMapping.put("montanttotaldepense", "montanttotaldepense");
	}

}
