package com.github.aiderpmsi.pimsdriver.db.vaadin.query;

import java.util.HashMap;

public class DBQueryMapping extends HashMap<Object, Object> {

	/** Serial id */
	private static final long serialVersionUID = 8490940689002383771L;

	public DBQueryMapping(Object[][] mappings) {
		super(mappings.length);
		
		// FIRST, CHECK THE ARGUMENTS
		for (Object[] mapping : mappings) {
			if (mapping.length != 2) {
				throw new IllegalArgumentException("Mappings hav to be an array of 2 elements arrays");
			} else if (mapping[0] == null || mapping[1] == null) {
				throw new IllegalArgumentException("Mappings can't be null");
			}
		}
		
		// IF ARGUMENTS ARE OK, FILLS THE MAPPINGS
		for (Object[] mapping : mappings) {
			put(mapping[0], mapping[1]);
		}
			
	}

}
