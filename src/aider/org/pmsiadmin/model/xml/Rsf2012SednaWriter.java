package aider.org.pmsiadmin.model.xml;

import java.io.OutputStream;

import aider.org.pmsi.parser.exceptions.PmsiWriterException;
import aider.org.pmsi.writer.Rsf2012Writer;

/**
 * Objet de transfert de données pour un objet de type {@link PmsiRSF2012Reader}
 * @author delabre
 *
 */
public class Rsf2012SednaWriter extends Rsf2012Writer {
	
	private PmsiSednaStreamRunner streamRunner;
	
	/**
	 * Construction de la connexion à la base de données à partir des configurations
	 * données
	 * @throws PmsiWriterException 
	 */
	public Rsf2012SednaWriter(OutputStream out, PmsiSednaStreamRunner streamRunner) throws PmsiWriterException {
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
	}
}
