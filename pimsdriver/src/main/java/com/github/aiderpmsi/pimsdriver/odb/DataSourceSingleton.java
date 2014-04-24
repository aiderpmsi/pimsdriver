package com.github.aiderpmsi.pimsdriver.odb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataSourceSingleton {

	private final static String location = "localhost";

	private final static String user = "pimsdriver";
	
	private final static String pwd = "pimsdriver";

	private DataSource dataSource;
	
	private static DataSourceSingleton singleton = null;
	
	protected DataSourceSingleton() {
		
			// BUILD THE PGSQL DSN
			StringBuilder dsn = new StringBuilder();
			dsn.append("jdbc:postgresql://").
			append(location).append(":").
			append("5432").append("/").
			append("pimsdriver");
			  
			// CONNECTION PROPERTIES (LOOK AT http://commons.apache.org/proper/commons-dbcp/configuration.html)
			Properties props = new Properties();
			props.setProperty("url", dsn.toString());
			props.setProperty("username", user);
			props.setProperty("password", pwd);
			props.setProperty("password", pwd);
			props.setProperty("driverClassName", "org.postgresql.Driver");
			props.setProperty("defaultAutoCommit", "false");
			props.setProperty("defaultReadOnly", "false");
			props.setProperty("defaultTransactionIsolation", "SERIALIZABLE");
			props.setProperty("initialSize", "2");

			// THE POOL WILL USE IT TO CREATE CONNECTIONS
			ConnectionFactory connectionFactory =
					new DriverManagerConnectionFactory(dsn.toString(), props);
			
			// WRAPS THE REAL CONNECTIONS FROM CONNECTIONFACTORY WITH THE POOLING FUNCTIONALITY
			PoolableConnectionFactory poolableConnectionFactory =
					new PoolableConnectionFactory(connectionFactory, null); 
			
			 // WRAPS THE ACTUAL POOL OF CONNECTIONS INTO A GENERIC POOL OBJECT
			 ObjectPool<PoolableConnection> connectionPool =
					 new GenericObjectPool<>(poolableConnectionFactory);

			 // SETS THE OBJECT POOL AS WRAPPER TO PORTABLECONNECTIONFACTORY
			 poolableConnectionFactory.setPool(connectionPool);

			 // CREATE THE DATASOURCE
			 dataSource = new PoolingDataSource<>(connectionPool);
	}
	
	public synchronized Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public static synchronized DataSourceSingleton getInstance() {
		if (singleton == null) {
			singleton = new DataSourceSingleton();
		}
		return singleton;
	}
		
}
