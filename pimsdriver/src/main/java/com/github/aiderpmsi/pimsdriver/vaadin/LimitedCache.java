package com.github.aiderpmsi.pimsdriver.vaadin;

import java.util.LinkedHashMap;
import java.util.Map;

public class LimitedCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 6745519568791619460L;
	int maxSize;
	
	public LimitedCache(int maxSize) {
		this.maxSize = maxSize;
	}
	
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> old) {
        return size() > maxSize;
    }
    
}
