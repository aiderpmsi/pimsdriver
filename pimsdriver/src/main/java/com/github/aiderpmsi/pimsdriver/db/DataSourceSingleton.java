package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import com.github.aiderpmsi.pimsdriver.processor.ProcessTask;

public class DataSourceSingleton {

	private static final HashMap<String, DataSource> dataSources = new HashMap<>();

	private static final HashMap<String, Future<Boolean>> futures = new HashMap<>();
	
	private static final Logger log = Logger.getLogger(DataSourceSingleton.class.toString());

	public static synchronized Connection getConnection(final ServletContext context) throws SQLException {
		// GET CONNECTION INFORMATIONS FROM CONTEXT
		String urlContext = null, userContext = null, passwordContext	= null;
		try {
			final InitialContext jndiContext = new InitialContext();
			urlContext = (String) jndiContext.lookup("com.github.aiderpmsi.pimsdriver.jdbcurl");
			userContext = (String) jndiContext.lookup("com.github.aiderpmsi.pimsdriver.jdbcuser");
			passwordContext = (String) jndiContext.lookup("com.github.aiderpmsi.pimsdriver.jdbcpwd");
		} catch (NamingException e) {
			throw new SQLException(e);
		}
		
		DataSource dataSource = null;
		synchronized (dataSources) {
			if ((dataSource = dataSources.get(urlContext)) == null) {
				// DATASOURCE DID NOT EXIST, WE HAVE TO INIT IT
				dataSource = initDataSource(urlContext, userContext, passwordContext);
				dataSources.put(urlContext, dataSource);
				// INIT CLEANERS AND PROCESSORS
				futures.put(urlContext, Executors.newSingleThreadExecutor().submit(new ProcessTask(context)));			
			}
		}
		
		final Connection con = dataSource.getConnection();
		// FORCE AUTO COMMIT TO FALSE
		con.setAutoCommit(false);
		// SETS TRANSACTION ISOLATION
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		return con;
	}

	private static DataSource initDataSource(final String url, final String user, final String pwd) throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}

		// CONNECTION PROPERTIES (LOOK AT http://commons.apache.org/proper/commons-dbcp/configuration.html)
		final Properties props = new Properties();
		props.setProperty("url", url);
		props.setProperty("user", user);
		props.setProperty("password", pwd);
		props.setProperty("driverClassName", "org.postgresql.Driver");
		props.setProperty("defaultAutoCommit", "false");
		props.setProperty("defaultReadOnly", "false");
		props.setProperty("defaultTransactionIsolation", "SERIALIZABLE");
		props.setProperty("initialSize", "2");
		props.setProperty("accessToUnderlyingConnectionAllowed", "true");

		// THE POOL WILL USE IT TO CREATE CONNECTIONS
		final ConnectionFactory connectionFactory =
				new DriverManagerConnectionFactory(url, props);
		
		// WRAPS THE REAL CONNECTIONS FROM CONNECTIONFACTORY WITH THE POOLING FUNCTIONALITY
		final PoolableConnectionFactory poolableConnectionFactory =
				new PoolableConnectionFactory(connectionFactory, null); 
		
		 // WRAPS THE ACTUAL POOL OF CONNECTIONS INTO A GENERIC POOL OBJECT
		final ObjectPool<PoolableConnection> connectionPool =
				new GenericObjectPool<>(poolableConnectionFactory);

		 // SETS THE OBJECT POOL AS WRAPPER TO PORTABLECONNECTIONFACTORY
		 poolableConnectionFactory.setPool(connectionPool);

		 // CREATE THE DATASOURCE
		 final DataSource dataSource = new PoolingDataSource<>(connectionPool);

		 // NOW CREATE THE DATABASE IF NEEDED
		 final Flyway flyway = new Flyway();
		 flyway.setDataSource(dataSource);

		 try {
			 flyway.migrate();
		 } catch (FlywayException e) {
			 e.printStackTrace();
			 throw new SQLException(e);
		 }
		 
		 return dataSource;
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
