package aider.org.pmsiadmin.model.xml;

import aider.org.pmsi.dto.PmsiPipedIOException;
import aider.org.pmsi.dto.Rss116PipedWriter;
import aider.org.pmsi.parser.PmsiRSS116Reader;

/**
 * Objet de transfert de données pour un objet de type {@link PmsiRSS116Reader}
 * @author delabre
 *
 */
public class Rss116SednaPipedWriter extends Rss116PipedWriter {

	PmsiSednaPipedReader reader;

	/**
	 * Construction de la connexion à la base de données à partir des configurations
	 * données
	 * @throws PmsiPipedIOException 
	 */
	public Rss116SednaPipedWriter(PmsiSednaPipedReader reader) throws PmsiPipedIOException {
		super(reader);
		this.reader = reader;
	}
	
	@Override
	public void writeStartDocument(String name, String[] attributes, String[] values) throws PmsiPipedIOException {
		String[] newAttributes = new String[attributes.length + 1];
		String[] newValues = new String[attributes.length + 1];
		System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
		System.arraycopy(values, 0, newValues, 0, attributes.length);
		
		newAttributes[attributes.length + 1] = "insertionTimeStamp";
		newValues[attributes.length + 1] = reader.getSednaTime();
		super.writeStartDocument(name, attributes, values);
	};
}
