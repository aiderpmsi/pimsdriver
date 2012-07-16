package aider.org.pmsiadmin.model.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;

import aider.org.pmsi.dto.DtoPmsiException;
import aider.org.pmsi.dto.DtoPmsiImpl;
import aider.org.pmsi.dto.DtoPmsiWriter;
import aider.org.pmsi.parser.linestypes.PmsiLineType;

/**
 * Implémente l'interface DtoPmsi pour transformer le flux pmsi en flux xml
 * @author delabre
 *
 */
public abstract class DtoPmsiXmlImpl extends DtoPmsiImpl {
	
	private SednaConnection connection = null;
	
	/**
	 * Construction.
	 * @throws DtoPmsiException 
	 */
	public DtoPmsiXmlImpl(SednaConnection connection) throws DtoPmsiException {
		super();
		this.connection = connection;
	}
	
	/**
	 * Méthode permettant de créer le thread lisant les données que la classe
	 * principale écrit pour les rediriger là où il faut (dans cette implémentation,
	 * dans une base de données sedna)
	 * @throws DtoPmsiException
	 */
	private void createThreadWriter() throws DtoPmsiException {
		threadWriter = new DtoPmsiXmlWriter(connection);
	}

	/**
	 * Libère toutes les ressources associées à ce dto
	 * @throws DtoPmsiException 
	 */
	public void close() throws DtoPmsiException{		
		// On ferme les resources de la classe parente, ce qui nous permet d'attendre
		// que l'écrivain ait fini son job
		try {
			super.close();
			// L'insertion a été un succès
			try {
				connection.commit();
			} catch (DriverException e) {
				throw new DtoPmsiException(e);
			}
		} catch (DtoPmsiException e) {
			// L'insertion a été un échec, il faut faire un rollback
			try {
				connection.rollback();
			} catch (DriverException e1) {
				throw new DtoPmsiException(e);
			}
		}
	}
}
