package aider.org.pmsiadmin.connector;

import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.model.xml.DtoFinessList;
import aider.org.pmsiadmin.model.xml.PmsiDtoSedna;
import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;

public class SednaConnector {

	public SednaConnection sednaConnection = null;
	
	public PmsiDtoSedna pmsiDtoSedna = null;
	
	public DtoFinessList dtoFinessList = null;
	
	public SednaConnector() {
	}

	public void open(Configuration config) throws DriverException {
		checkIsDeconnected();
		
		sednaConnection = DatabaseManager.getConnection(
				config.getSednaHost(),
				config.getSednaDb(),
				config.getSednaUser(),
				config.getSednaPwd());
	}
	
	public void close() throws DriverException {
		checkIsConnected();
		
		if (pmsiDtoSedna != null)
			pmsiDtoSedna = null;
		
		sednaConnection.close();
		sednaConnection = null;
	}
	
	public PmsiDtoSedna getPmsiDtoSedna() throws DriverException {
		checkIsConnected();
		
		if (pmsiDtoSedna == null)
			pmsiDtoSedna = new PmsiDtoSedna(sednaConnection);
		
		return pmsiDtoSedna;
	}
	
	public DtoFinessList getDtoFinessList() throws DriverException {
		checkIsConnected();
		
		if (dtoFinessList == null)
			dtoFinessList = new DtoFinessList(sednaConnection);
		
		return dtoFinessList;
			
	}
	
	public void begin() throws DriverException {
		checkIsConnected();
		
		sednaConnection.begin();
	}
	
	public void rollback() throws DriverException {
		checkIsConnected();
		
		sednaConnection.rollback();
	}
	
	public void commit() throws DriverException {
		checkIsConnected();
		
		sednaConnection.commit();
	}
	
	private void checkIsDeconnected() throws DriverException {
		if (sednaConnection != null)
			throw new DriverException("Sedna déjà connectée", 10001);
	}
	
	private void checkIsConnected() throws DriverException {
		if (sednaConnection == null)
			throw new DriverException("Sedna non connectée", 10002);
	}
}
