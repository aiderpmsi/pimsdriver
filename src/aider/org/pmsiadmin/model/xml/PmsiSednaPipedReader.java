package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.InputStream;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

import aider.org.pmsi.dto.PmsiPipedReaderImpl;
import aider.org.pmsi.parser.exceptions.PmsiPipedIOException;

/**
 * Classe permettant de lire le flux écrit par le thread principal pour l'écrire où il
 * elle le veut
 * @author delabre
 *
 */
public class PmsiSednaPipedReader extends PmsiPipedReaderImpl {
	
	private SednaConnection connection;
		
	private String pmsiDocNumber;
	
	private String sednaTime;
	
	public PmsiSednaPipedReader(SednaConnection connection) throws PmsiPipedIOException {
		super();
		this.connection = connection;
		// Début de transaction
		try {
			connection.begin();
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
		// Définition du numéro de document
		setPmsiDocNumber();
		// Définition de l'heure de sedna
		setSednaTime();
	}
	
	@Override
	protected void writeInputStream(InputStream input) throws PmsiPipedIOException {
		try {
			SednaStatement st = connection.createStatement();
			st.loadDocument(input, "pmsi-" + getPmsiDocNumber(), "Pmsi");
		} catch (DriverException e) {
			if (e.getErrorCode() == 168)
				// Le  fichier est mal formé 
				throw new PmsiPipedIOException("Malformed file");
			else
				throw new PmsiPipedIOException(e);
		} catch (IOException e) {
			throw new PmsiPipedIOException(e);
		}
	}
	
	/**
	 * Récupère dans Sedna le numéro de document pmsi
	 * @throws DtoPmsiException 
	 */
	private void setPmsiDocNumber() throws PmsiPipedIOException {
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
			throw new PmsiPipedIOException(e);
		}
	}
	
	public String getPmsiDocNumber() {
		return pmsiDocNumber;
	}
	
	/**
	 * Récupère dans sedna l'heure de la db
	 * @throws DtoPmsiException 
	 */
	private void setSednaTime() throws PmsiPipedIOException {
		try {
			SednaStatement st = connection.createStatement();
			st.execute("current-dateTime()");
			SednaSerializedResult pr = st.getSerializedResult();
			sednaTime = pr.next();
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
	}
	
	/**
	 * Retourne le datetime récupéré auprès de sedna
	 * @return
	 */
	public String getSednaTime() {
		return sednaTime;
	}
	
	@Override
	public void close() throws PmsiPipedIOException {
		// Si l'insertion a échoué, il faut faire un roolback, sinon
		// un commit
		try {
			if (getStatus() == false)
				connection.rollback();
			else
				connection.commit();
		} catch (DriverException e) {
			if (e.getErrorCode() == 411)
				// Il n'y a pas de transaction à annuler : c'est plus une info qu'une erreur à cet endroit
				return;
			else
				throw new PmsiPipedIOException(e);
		}
		super.close();
	}
}
