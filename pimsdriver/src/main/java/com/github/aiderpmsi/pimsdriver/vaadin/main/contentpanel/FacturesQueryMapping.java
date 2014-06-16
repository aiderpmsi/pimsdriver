package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.HashMap;

public class FacturesQueryMapping {

	public static final HashMap<Object, Object> sqlMapping = new HashMap<>();
	
	static {
		sqlMapping.put("pmel_id", "pmel_id");
		sqlMapping.put("pmel_root", "pmel_root");
		sqlMapping.put("ligne", "pmel_line");
		sqlMapping.put("numfacture", "numfacture");
		sqlMapping.put("numrss", "numrss");
		sqlMapping.put("codess", "codess");
		sqlMapping.put("sexe", "sexe");
		sqlMapping.put("datenaissance", "datenaissance");
		sqlMapping.put("dateentree", "dateentree");
		sqlMapping.put("datesortie", "datesortie");
		sqlMapping.put("totalfacturehonoraire", "totalfacturehonoraire");
		sqlMapping.put("totalfactureph", "totalfactureph");
		sqlMapping.put("etatliquidation", "etatliquidation");
	}

}
