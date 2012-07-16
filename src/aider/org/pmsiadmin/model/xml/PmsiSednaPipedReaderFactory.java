package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import aider.org.pmsi.dto.PmsiPipedIOException;
import aider.org.pmsi.dto.PmsiPipedReader;
import aider.org.pmsi.dto.PmsiPipedReaderFactory;
import aider.org.pmsi.parser.PmsiReader;
import aider.org.pmsiadmin.config.Configuration;

/**
 * Classe créant le dto adapté à chaque PmsiReader
 * @author delabre
 *
 */
public class PmsiSednaPipedReaderFactory extends PmsiPipedReaderFactory {
	
	private Configuration config = null;
	
	/**
	 * Constructeur par défaut, ne fait rien
	 * @throws DriverException
	 */
	public PmsiSednaPipedReaderFactory(Configuration config) throws PmsiPipedIOException {
		this.config = config;
	}
	
	/**
	 * Crée un objet de tranfert de données et le renvoie
	 * @param reader le lecteur de pmsi ayant besoin de cet objet
	 * @return L'objet de transfert de donné adapté
	 * @throws PmsiPipedIOException
	 */
	public PmsiPipedReader getPmsiPipedReader(PmsiReader<?, ?> reader) throws PmsiPipedIOException {
		SednaConnection connection = null;
		try {
			connection = DatabaseManager.getConnection(
					config.getSednaHost(),
					config.getSednaDb(),
					config.getSednaUser(),
					config.getSednaPwd());
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
		
		return new PmsiSednaPipedReader(connection);
	}
	
	/**
	 * Libère les resource associées à cette fabrique
	 */
	public void close() {
	}
}
