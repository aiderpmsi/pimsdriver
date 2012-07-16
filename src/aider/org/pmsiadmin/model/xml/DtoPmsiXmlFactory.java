package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import aider.org.pmsi.dto.DtoPmsi;
import aider.org.pmsi.dto.DtoPmsiException;
import aider.org.pmsi.dto.DtoPmsiFactory;
import aider.org.pmsi.parser.PmsiRSF2009Reader;
import aider.org.pmsi.parser.PmsiRSF2012Reader;
import aider.org.pmsi.parser.PmsiRSS116Reader;
import aider.org.pmsi.parser.PmsiReader;
import aider.org.pmsiadmin.config.Configuration;

public class DtoPmsiXmlFactory extends DtoPmsiFactory {

	private Configuration config = null;
	
	public DtoPmsiXmlFactory(Configuration config) throws DtoPmsiException {
		super();
		this.config = config;
	}

	@Override
	public DtoPmsi getDtoPmsiLineType(PmsiReader<?, ?> reader) throws DtoPmsiException {
		// Variables de méthode
		SednaConnection connection = null;
		try {
			connection = DatabaseManager.getConnection(
					config.getSednaHost(),
					config.getSednaDb(),
					config.getSednaUser(),
					config.getSednaPwd());
		} catch (DriverException e) {
			throw new DtoPmsiException(e);
		}
		if (reader instanceof PmsiRSF2009Reader) {
			return new DtoXmlRsf2009(connection);
		} else if (reader instanceof PmsiRSF2012Reader) {
			return new DtoXmlRsf2012(connection);
		} else if (reader instanceof PmsiRSS116Reader) {
			return new DtoXmlRss116(connection);
		}
		return null;
	}
	
	/**
	 * Libère les resource associées à cette fabrique
	 */
	public void close() {
	}
	
}
