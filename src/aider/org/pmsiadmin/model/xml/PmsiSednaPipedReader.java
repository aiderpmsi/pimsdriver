package aider.org.pmsiadmin.model.xml;

import java.io.InputStream;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

import aider.org.pmsi.dto.PmsiPipedIOException;
import aider.org.pmsi.dto.PmsiPipedReaderImpl;

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
	public void run() {
		try {
			writeInputStream(getPipedInputStream());
			setStatus(true);
		} catch (PmsiPipedIOException e) {
			setStatus(false);
			setTerminalException(e);
		} finally {
			try {
				getSemaphore().release();
			} catch (PmsiPipedIOException e) {
				setStatus(false);
				setTerminalException(e);
			}
		}
	}
	
	/**
	 * Ecrit les données de l'inputstream là où c'est nécessaire
	 * (peut être surchargé pour écrire dans une base de données)
	 * @throws DtoPmsiException si l'écriture a été un éches
	 */
	protected void writeInputStream(InputStream input) throws PmsiPipedIOException {
		try {
			SednaStatement st = connection.createStatement();
			st.loadDocument(input, "pmsi-" + getPmsiDocNumber(), "Pmsi");
		} catch (Exception e) {
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
			throw new PmsiPipedIOException(e);
		}
		super.close();
	}
}
