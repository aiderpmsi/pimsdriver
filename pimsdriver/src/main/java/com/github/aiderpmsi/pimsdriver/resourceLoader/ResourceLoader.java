package com.github.aiderpmsi.pimsdriver.resourceLoader;

import javax.ws.rs.core.StreamingOutput;

public class ResourceLoader {

	private static ResourceLoader instance = null;
	
	protected ResourceLoader() {
		// Singleton
	}
	
	public synchronized StreamingOutput getResource(String name) {
		return null;
	}

}
