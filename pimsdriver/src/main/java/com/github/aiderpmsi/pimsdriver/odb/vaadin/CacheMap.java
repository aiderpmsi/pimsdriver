package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Derived from vaadin CacheMap
 * @author delabre
 *
 * @param <K> Key
 * @param <V> Value
 */
class CacheMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 5546747960943264172L;
	private int cacheLimit = ODBContainer.CACHE_RATIO
            * ODBContainer.DEFAULT_PAGE_LENGTH;

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheLimit;
    }

    void setCacheLimit(int limit) {
        cacheLimit = limit > 0 ? limit : ODBContainer.DEFAULT_PAGE_LENGTH;
    }

    int getCacheLimit() {
        return cacheLimit;
    }
}