package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import aider.org.pmsi.dto.PmsiDto;
import aider.org.pmsi.parser.exceptions.PmsiPipedIOException;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

public class PmsiDtoSedna implements PmsiDto {
	
	private SednaConnection sednaConnection;
		
	private String pmsiDocNumber;
	
	private String sednaTime;
	
	private String finess = null;
	
	private String root1= null;
	
	private String root2 = null;
	
	public PmsiDtoSedna(SednaConnection sednaConnection) throws PmsiPipedIOException {
		this.sednaConnection = sednaConnection;
		// Début de transaction
		try {
			sednaConnection.begin();
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
		// Définition du numéro de document
		setPmsiDocNumber();
		// Définition de l'heure de sedna
		setSednaTime();
	}
	
	@Override
	public void close() throws PmsiPipedIOException {
		// Si l'insertion a échoué, il faut faire un roolback, sinon
		// un commit
		try {
			sednaConnection.close();
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
	}

	@Override
	public void writePmsi(InputStream input) throws PmsiPipedIOException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.loadDocument(input, "pmsi-" + getPmsiDocNumber(), "Pmsi");
			// Récupération de la racine de ce pmsi
			setUniqueElements("pmsi-" + getPmsiDocNumber(), "Pmsi");

			// Vérification 1 : Si rsf2012 / 2009, vérification qu'il n'existe pas d'autre
			// rsf2010 ou 2009 à cette date d'insertion pour ce finess et ce mois de fin
			// de finess

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
			SednaStatement st = sednaConnection.createStatement();
			st.execute("update \n" +
			"replace $l in fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice \n" +
			"with <indice>{$l/text() + 1}</indice>");
	
			st = sednaConnection.createStatement();
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
			SednaStatement st = sednaConnection.createStatement();
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
	
	/**
	 * Récupère dans la base de données les éléments permettant d'identifier de manière unique 
	 * un document par ses éléments
	 * @param doc
	 * @param collection
	 * @throws PmsiPipedIOException
	 */
	private void setUniqueElements(String doc, String collection) throws PmsiPipedIOException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.execute("for $i in fn:doc(\"" + doc + "\", \"" + collection + "\")/(*[1])/(*[1])\n" +
					"return <entry type = \"{name($i/..)}\" headertype = \"{name($i)}\" finess = \"{string($i/@Finess)}\" />");
			
			SednaSerializedResult pr = st.getSerializedResult();
			String result = pr.next();
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			XMLStreamReader xmlsr = xmlif.createXMLStreamReader(new StringReader(result));
			
			int eventType;
			while (xmlsr.hasNext()) {
				eventType = xmlsr.next();
				if (eventType == XMLEvent.START_ELEMENT) {
					int attCount = xmlsr.getAttributeCount();
					for (int i = 0 ; i < attCount ; i++) {
						if (xmlsr.getAttributeLocalName(i).equals("type"))
							root1 = xmlsr.getAttributeValue(i);
						else if (xmlsr.getAttributeLocalName(i).equals("headertype"))
							root2 = xmlsr.getAttributeValue(i);
						else if (xmlsr.getAttributeLocalName(i).equals("finess"))
							finess = xmlsr.getAttributeValue(i);
					}
				}
			}
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		} catch (XMLStreamException e) {
			throw new PmsiPipedIOException(e);
		}
	}
	
	public void commit() throws PmsiPipedIOException {
		try {
			sednaConnection.commit();
		} catch (DriverException e) {
			throw new PmsiPipedIOException(e);
		}
	}
	
	public void rollback() throws PmsiPipedIOException {
		try {
			sednaConnection.rollback();
		} catch (DriverException e) {
			if (e.getErrorCode() == 411)
				// Il n'y a pas de rollback, c'est plus une info qu'une erreur
				return;
			else
				throw new PmsiPipedIOException(e);
		}
	}
}
