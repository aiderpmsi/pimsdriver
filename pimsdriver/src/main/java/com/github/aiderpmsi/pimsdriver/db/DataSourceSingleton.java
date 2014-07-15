package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import com.github.pjpo.pimsdriver.datasource.DataSourceProvider;

public class DataSourceSingleton {

	private static final HashMap<String, DataSource> dataSources = new HashMap<>();

	private static final HashMap<String, Future<Boolean>> futures = new HashMap<>();
	
	private static final Logger log = Logger.getLogger(DataSourceSingleton.class.toString());

	public static synchronized Connection getConnection(final ServletContext context) throws SQLException {
		try {
			InitialContext jndiContext = new InitialContext();
			DataSourceProvider bi = (DataSourceProvider) jndiContext.lookup("java:global/business/datasource-0.0.1-SNAPSHOT/DataSourceProviderBean!com.github.pjpo.pimsdriver.datasource.DataSourceProvider");
			Connection con = bi.getConnection();
			return con;
		} catch (Throwable e) {
			throw new SQLException(e);
		}
	}
	
	public static void clean() {
		// STOPS EACH FUTURE AND CLEANS HASMAPS
		synchronized (dataSources) {
			for (final Iterator<Entry<String, Future<Boolean>>> futuresIt = futures.entrySet().iterator();futuresIt.hasNext();) {
				final Entry<String, Future<Boolean>> futureEntry = futuresIt.next();
				try {
					futureEntry.getValue().cancel(true);
					futureEntry.getValue().get();
				} catch (InterruptedException | ExecutionException | CancellationException e) {
					log.warning(e.getMessage());
				}
				futuresIt.remove();
				// REMOVE THE DATABASE ENTRY TOO
				dataSources.remove(futureEntry.getKey());
			}
		}
	}
}
