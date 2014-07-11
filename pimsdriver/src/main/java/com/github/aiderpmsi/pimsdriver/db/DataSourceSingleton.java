package com.github.aiderpmsi.pimsdriver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.glassfish.jersey.server.spi.Container;

import com.github.aiderpmsi.pimsdriver.processor.ProcessTask;

public class DataSourceSingleton {

	private static final HashMap<String, DataSource> dataSources = new HashMap<>();

	public static synchronized Connection getConnection(final ServletContext context) throws SQLException {
		// GET CONNECTION INFORMATIONS FROM CONTEXT
		String url = null;
		try {
			final InitialContext jndiContext = new InitialContext();
			Object urlContext = jndiContext.lookup("com.github.aiderpmsi.pimsdriver.jdbcurl");
			url = (String) urlContext;
		} catch (NamingException e) {
			throw new SQLException(e);
		}
		
		DataSource dataSource = null;
		if ((dataSource = dataSources.get(url)) == null) {
			// DATASOURCE DID NOT EXIST, WE HAVE TO INIT IT
			dataSource = initDataSource("", "", "");
			dataSources.put(url, dataSource);
			// INIT CLEANERS AND PROCESSORS
			
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
	
	private void initDaemons(final ServletContext context) throws SQLException {
		ExecutorService execute = Executors.newSingleThreadExecutor();
		threadResult = execute.submit(new ProcessTask());
	}

	@Override
	public void onReload(Container container) {
		// STOP AND START
		onShutdown(container);
		onStartup(container);
	}

	@Override
	public void onShutdown(Container container) {
		threadResult.cancel(true);
		try {
			threadResult.get();
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			log.warning(e.getMessage());
		} finally {
			// DO NOTHING
		}

	}

	public static void clean() {
		// STOPS EACH FUTURE AND CLEANS HASMAPS
		
	}
}
