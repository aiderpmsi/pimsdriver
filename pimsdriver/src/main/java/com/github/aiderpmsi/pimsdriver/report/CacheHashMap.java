package com.github.aiderpmsi.pimsdriver.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheHashMap<K, V> extends LinkedHashMap<K, V> {

	/** Serial id */
	private static final long serialVersionUID = 6708275281548480232L;

	private int n;

	public CacheHashMap(int n) {
		super(n);
		this.n = n;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
		return size() > n;
	}

}
