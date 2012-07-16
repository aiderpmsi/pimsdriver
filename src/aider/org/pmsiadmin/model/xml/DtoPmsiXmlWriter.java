package aider.org.pmsiadmin.model.xml;

import java.io.InputStream;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
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
		
	private String pmsiDocNumber;
	
	public DtoPmsiXmlWriter(SednaConnection connection) throws DtoPmsiException {
		super();
		this.connection = connection;
	}

	/**
	 * Ecrit les données de l'inputstream là où c'est nécessaire
	 * (peut être surchargé pour écrire dans une base de données)
	 * @throws DtoPmsiException si l'écriture a été un éches
	 */
	protected void writeInputStream(InputStream input) throws DtoPmsiException {
		try {
			// Définition du numéro de document
			setPmsiDocNumber();
			
			SednaStatement st = connection.createStatement();
			st.loadDocument(input, "pmsi-" + getPmsiDocNumber(), "Pmsi");
		} catch (Exception e) {
			throw new DtoPmsiException(e);
		}
	}
	
	/**
	 * Récupère dans Sedna le numéro de document pmsi
	 * @throws DtoPmsiException 
	 */
	private void setPmsiDocNumber() throws DtoPmsiException {
		try {
			SednaStatement st = connection.createStatement();
			st.execute("update \n" +
			"replace $l in fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice \n" +
			"with <indice>{$l/text() + 1}</indice>");
	
			st = connection.createStatement();
			st.execute("fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice/text()");
			SednaSerializedResult pr = st.getSerializedResult();
			pmsiDocNumber = pr.next();
		} catch (DriverException e) {
			throw new DtoPmsiException(e);
		}
	}
	
	public String getPmsiDocNumber() {
		return pmsiDocNumber;
	}
}
