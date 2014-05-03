package com.github.aiderpmsi.pimsdriver.vaadin.utils;

import java.util.HashMap;

public class UploadPmsiMapping {

	public static final HashMap<Object, Object> sqlMapping = new HashMap<>();
	
	static {
		sqlMapping.put("finess", "plud_finess");
		sqlMapping.put("year", "plud_year");
		sqlMapping.put("month", "plud_month");
		sqlMapping.put("dateenvoi", "plud_dateenvoi");
		sqlMapping.put("processed", "plud_processed");
	}

}
