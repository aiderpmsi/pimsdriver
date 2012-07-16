package aider.org.pmsiadmin.model.xml;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaStatement;

import aider.org.pmsi.dto.DtoPmsiException;
import aider.org.pmsi.dto.DtoPmsiWriter;

/**
 * Classe permettant de lire le flux écrit par le thread principal pour l'écrire où il
 * elle le veut
 * @author delabre
 *
 */
public class DtoPmsiXmlWriter extends DtoPmsiWriter {
	
	private SednaConnection connection;
	
	public DtoPmsiXmlWriter(SednaConnection connection) throws DtoPmsiException {
		super();
		this.connection = connection;
	}

	/**
	 * Ecrit les données de l'inputstream là où c'est nécessaire
	 * (peut être surchargé pour écrire dans une base de données)
	 * @throws DtoPmsiException si l'écriture a été un éches
	 */
	private void writeInputStream(InputStream input) throws DtoPmsiException {
		try {
			// Récupération de l'heure de connection
			
			SednaStatement st = connection.createStatement();
			st.loadDocument(input, "pmsi-10" + numDocument, "Pmsi");
		} catch (Exception e) {
			throw new DtoPmsiException(e);
		}
	}
}
