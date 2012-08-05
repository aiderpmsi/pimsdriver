package aider.org.pmsiadmin.model.xml;

import java.io.OutputStream;

import aider.org.pmsi.exceptions.PmsiWriterException;
import aider.org.pmsi.writer.Rss116Writer;

/**
 * Objet de transfert de données pour un objet de type {@link PmsiRSS116Reader}
 * @author delabre
 *
 */
public class Rss116SednaWriter extends Rss116Writer {

	
	private PmsiSednaStreamRunner streamRunner;
	
	/**
	 * Construction de la connexion à la base de données à partir des configurations
	 * données
	 * @throws PmsiWriterException 
	 */
	public Rss116SednaWriter(OutputStream out, PmsiSednaStreamRunner streamRunner) throws PmsiWriterException {
		super(out);
		this.streamRunner = streamRunner;
	}
	
	@Override
	public void writeStartDocument(String name, String[] attributes, String[] values) throws PmsiWriterException {
		String[] newAttributes = new String[attributes.length + 1];
		String[] newValues = new String[attributes.length + 1];
		System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
		System.arraycopy(values, 0, newValues, 0, attributes.length);
		
		newAttributes[attributes.length] = "insertionTimeStamp";
		newValues[attributes.length] = streamRunner.getSednaTime();
		super.writeStartDocument(name, newAttributes, newValues);
	};
}
