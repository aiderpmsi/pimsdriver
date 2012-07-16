package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

import aider.org.pmsi.dto.DtoPmsiException;
import aider.org.pmsi.dto.DtoPmsiImpl;

/**
 * Implémente l'interface DtoPmsi pour transformer le flux pmsi en flux xml
 * @author delabre
 *
 */
public abstract class DtoPmsiXmlImpl extends DtoPmsiImpl {
	
	private SednaConnection connection = null;
	
	private String sednaTime;
	
	/**
	 * Construction.
	 * @throws DtoPmsiException 
	 */
	public DtoPmsiXmlImpl(SednaConnection connection) throws DtoPmsiException {
		super();
		this.connection = connection;
		// Récupération de l'heure de sedna
		setSednaTime();
	}
	
	/**
	 * Surcharge l'écriture du début de document pour définir des attributs spécifiques
	 * @param name
	 * @param attributes
	 * @param values
	 * @throws DtoPmsiException
	 */
	public void writeStartDocument(String name, String[] attributes, String[] values) throws DtoPmsiException {
		String[] newAttributes = new String[attributes.length + 1];
		String[] newValues = new String[attributes.length + 1];
		System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
		System.arraycopy(values, 0, newValues, 0, attributes.length);
		
		newAttributes[attributes.length + 1] = "insertionTimeStamp";
		newValues[attributes.length + 1] = getSednaTime();
	}

	/**
	 * Méthode permettant de créer le thread lisant les données que la classe
	 * principale écrit pour les rediriger là où il faut (dans cette implémentation,
	 * dans une base de données sedna)
	 * @throws DtoPmsiException
	 */
	protected void createThreadWriter() throws DtoPmsiException {
		threadWriter = new DtoPmsiXmlWriter(connection);
	}

	/**
	 * Récupère l'heure de la db Sedna et la stocke dans la db
	 * @throws DtoPmsiException 
	 */
	private void setSednaTime() throws DtoPmsiException {
		try {
			SednaStatement st = connection.createStatement();
			st.execute("current-dateTime()");
			SednaSerializedResult pr = st.getSerializedResult();
			sednaTime = pr.next();
		} catch (DriverException e) {
			throw new DtoPmsiException(e);
		}
	}
	
	/**
	 * Retourne le datetime récupéré auprès de sedna
	 * @return
	 */
	public String getSednaTime() {
		return sednaTime;
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
