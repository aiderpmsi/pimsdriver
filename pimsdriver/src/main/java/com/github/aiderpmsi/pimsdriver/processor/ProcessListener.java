package com.github.aiderpmsi.pimsdriver.processor;

import java.util.logging.Logger;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;

@Provider
public class ProcessListener implements ContainerLifecycleListener {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ProcessListener.class.toString());
	
	@Override
	public void onStartup(Container container) {
		// LET BET EACH DATASOURCE BE INITIALIZED WHEN FIRST CALL TO IT WILL BE DONE
	}

	@Override
	public void onReload(Container container) {
		// STOP AND START
		onShutdown(container);
		onStartup(container);
	}

	@Override
	public void onShutdown(Container container) {
		// STOPS EACH PROCESS FOR EACH DATASOURCE
		DataSourceSingleton.clean();
	}
}