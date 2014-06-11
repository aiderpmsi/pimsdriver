package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.SQLException;

public interface StatementProvider {
	
	public class Entry<T> {
		public T object;
		public Class<? extends T> clazz;
		
		public Entry() {
			object = null;
			clazz = null;
		}
		
		public Entry(T object, Class<? extends T> clazz) {
			this.object = object;
			this.clazz = clazz;
		}
	}
	
	public String getStatement(Entry<?>...entries) throws SQLException;
	
}
