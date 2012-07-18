package aider.org.pmsiadmin.model.xml;

import aider.org.pmsi.dto.PmsiThreadedPipedReaderImpl;
import aider.org.pmsi.parser.exceptions.PmsiPipedIOException;

/**
 * Classe permettant de lire le flux écrit par le thread principal pour l'écrire où il
 * elle le veut
 * @author delabre
 *
 */
public class PmsiSednaPipedReader extends PmsiThreadedPipedReaderImpl {

	private PmsiDtoSedna pmsiDtoSedna = null;
	
	public PmsiSednaPipedReader(PmsiDtoSedna pmsiDtoSedna) throws PmsiPipedIOException {
		super(pmsiDtoSedna);
		this.pmsiDtoSedna = pmsiDtoSedna;
	}
	
	public String getPmsiDocNumber() {
		return pmsiDtoSedna.getPmsiDocNumber();
	}
		
	/**
	 * Retourne le datetime récupéré auprès de sedna
	 * @return
	 */
	public String getSednaTime() {
		return pmsiDtoSedna.getSednaTime();
	}
	
	@Override
	public void close() throws PmsiPipedIOException {
		// Si l'insertion a échoué, il faut faire un roolback, sinon
		// un commit
		if (getStatus() == false)
			pmsiDtoSedna.rollback();
		else
			pmsiDtoSedna.commit();
		
		// Fermeture de la classe parente
		super.close();
	}
}
