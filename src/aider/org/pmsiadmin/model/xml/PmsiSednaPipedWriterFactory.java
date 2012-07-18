package aider.org.pmsiadmin.model.xml;

import aider.org.pmsi.dto.PmsiPipedWriter;
import aider.org.pmsi.dto.PmsiPipedWriterFactory;
import aider.org.pmsi.parser.PmsiRSF2009Reader;
import aider.org.pmsi.parser.PmsiRSF2012Reader;
import aider.org.pmsi.parser.PmsiRSS116Reader;
import aider.org.pmsi.parser.PmsiReader;
import aider.org.pmsi.parser.exceptions.PmsiPipedIOException;

/**
 * Classe créant le PipedWriter associé à chaque objet de type {@link PmsiReader}
 * @author delabre
 *
 */
public class PmsiSednaPipedWriterFactory extends PmsiPipedWriterFactory {

	/**
	 * Construit à partir de la définition d'une fabrique de {@link PmsiPipedReader}
	 * l'objet.
	 * @param pmsiPipedReaderFactory : la fabrique qui sera utilisée pour générer les
	 *   {@link PmsiPipedReader} utilisés par les {@link PmsiPipedWriter}
	 * @throws DriverException
	 */
	public PmsiSednaPipedWriterFactory(PmsiSednaPipedReaderFactory pmsiPipedReaderFactory) throws PmsiPipedIOException {
		super(pmsiPipedReaderFactory);
	}
	
	/**
	 * Crée un {@link PmsiPipedWriter} adapté au reader {@link PmsiReader}
	 * @param reader le lecteur de pmsi ayant besoin de cet objet
	 * @return L'écrivain adapté au type de fichier
	 * @throws PmsiPipedIOException
	 */
	public PmsiPipedWriter getPmsiPipedWriter(PmsiReader<?, ?> reader) throws PmsiPipedIOException {
		PmsiSednaPipedReader pmsiPipedReader = (PmsiSednaPipedReader) ((PmsiSednaPipedReaderFactory) getPmsiPipedReaderFactory()).getPmsiPipedReader(reader);
		if (reader instanceof PmsiRSF2009Reader) {
			return new Rsf2009SednaPipedWriter(pmsiPipedReader);
		} else if (reader instanceof PmsiRSF2012Reader) {
			return new Rsf2012SednaPipedWriter(pmsiPipedReader);
		} else if (reader instanceof PmsiRSS116Reader) {
			return new Rss116SednaPipedWriter(pmsiPipedReader);
		}
		return null;
	}
	
	/**
	 * Libère les resource associées à cette fabrique
	 */
	public void close() {
	}
}
