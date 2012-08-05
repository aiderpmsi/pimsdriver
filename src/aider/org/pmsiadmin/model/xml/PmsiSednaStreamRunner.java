package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

import aider.org.pmsi.dto.PmsiDtoRunnable;
import aider.org.pmsi.exceptions.PmsiDtoRunnableException;
import aider.org.pmsiadmin.config.Configuration;

public class PmsiSednaStreamRunner implements PmsiDtoRunnable {

	private InputStream inputStream;
	
	private SednaConnection sednaConnection;
	
	private String docNumber = null;
	
	private String sednaTime = null;
	
	private String report = null;
	
	public PmsiSednaStreamRunner(InputStream inputStream, Configuration configuration) throws PmsiDtoRunnableException {
		this.inputStream = inputStream;
		
		// Connection à la base sedna
		try {
			sednaConnection = DatabaseManager.getConnection(
					configuration.getSednaHost(),
					configuration.getSednaDb(),
					configuration.getSednaUser(),
					configuration.getSednaPwd());
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}

		// Début de transaction
		try {
			sednaConnection.begin();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
		
		// Définition du numéro de document
		setPmsiDocNumber();
		
		// Définition de l'heure de sedna
		setSednaTime();
	}
	
	@Override
	public void run() throws PmsiDtoRunnableException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.loadDocument(inputStream, "pmsi-" + getPmsiDocNumber(), "Pmsi");
			
			// Lancement du rapport
			setReport();
			
			// Parsing du rapport
			XmlReport xmlReport = new XmlReport(getReport());
			
			if (xmlReport.getCountFinessErrors() != null && xmlReport.getCountFinessErrors() != 0 ||
					xmlReport.getCountIdentityErrors() != null && xmlReport.getCountIdentityErrors() != 0 ||
							xmlReport.getCountNumFactureErrors() != null && xmlReport.getCountNumFactureErrors() != 0)
				throw new PmsiDtoRunnableException(getReport());
			
		} catch (DriverException e) {
			if (e.getErrorCode() == 168)
				// Le  fichier est mal formé 
				throw new PmsiDtoRunnableException("Malformed file", e);
			else
				throw new PmsiDtoRunnableException(e);
		} catch (IOException e) {
			throw new PmsiDtoRunnableException(e);
		} catch (XMLStreamException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}

	/**
	 * Récupère dans Sedna le numéro de document pmsi
	 * @throws DtoPmsiException 
	 */
	private void setPmsiDocNumber() throws PmsiDtoRunnableException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.execute("update \n" +
			"replace $l in fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice \n" +
			"with <indice>{$l/text() + 1}</indice>");
	
			st = sednaConnection.createStatement();
			st.execute("fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice/text()");
			SednaSerializedResult pr = st.getSerializedResult();
			docNumber = pr.next();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}

	public synchronized String getPmsiDocNumber() {
		return docNumber;
	}
	
	/**
	 * Récupère dans sedna l'heure de la db
	 * @throws  
	 */
	private void setSednaTime() throws PmsiDtoRunnableException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.execute("current-dateTime()");
			SednaSerializedResult pr = st.getSerializedResult();
			sednaTime = pr.next();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}
	
	/**
	 * Retourne le datetime récupéré auprès de sedna
	 * @return
	 */
	public synchronized String getSednaTime() {
		return sednaTime;
	}
	
	public void setReport() throws PmsiDtoRunnableException {
		try {
			SednaStatement st = sednaConnection.createStatement();
			st.execute("(for $i in fn:doc(\"pmsi-" + getPmsiDocNumber() + "\", \"Pmsi\")/(*[1])/(*[1])\n" +
			"return if (name($i/..) = \"RSF2012\" or (name($i/..) = \"RSF2009\"))\n" +
			"then\n" +
			"<result>\n" +
			"  <parent>\n" +
			"    <doc type = \"{name($i/..)}\"\n" +
			"         headertype = \"{name($i)}\"\n" +
			"         finess = \"{string($i/@Finess)}\"\n" +
			"         insertion = \"{string($i/../@insertionTimeStamp)}\"\n" +
			"         finperiode = \"{string($i/@DateFin)}\"/>\n" +
			"  </parent> {\n" +
			"  let $items:=fn:collection(\"Pmsi\")/\n" +
			"    (RSF2012 | RSF2009)[string(@insertionTimeStamp) = string($i/../@insertionTimeStamp)]/\n" +
			"      RsfHeader[. != $i and string(@Finess) = string($i/@Finess)]\n" +
			"      return <identityerrors count=\"{count($items)}\">{\n" +
			"        for $l in $items\n" +
			"        return\n" +
			"          <doc type = \"{name($l/..)}\" headertype = \"{name($l)}\" finess = \"{string($l/@Finess)}\" insertion = \"{string($l/../@insertionTimeStamp)}\"/>\n" +
			"      }</identityerrors> \n" +
			"} {\n" +
			"  let $rsfcontent:=$i/(RsfA | RsfA/(RsfB | RsfC | RsfH | RsfM))[@Finess != $i/@Finess]\n" +
			"  return <finesserrors count=\"{count($rsfcontent)}\"> {\n" +
			"    for $rsf in $rsfcontent \n" +
			"    return\n" +
			"    <rsf type=\"{name($rsf)}\" numfacture=\"{$rsf/@NumFacture}\"/>\n" +
			"  }\n" +
			"  </finesserrors> \n" +
			"} {\n" +
			"  let $rsfchildren:=$i/RsfA/(RsfB | RsfC | RsfH | RsfM)[@NumFacture != ../@NumFacture]\n" +
			"  return <numfactureerrors count=\"{count($rsfchildren)}\"> {\n" +
			"    for $rsf in $rsfchildren\n" +
			"    return\n" +
			"    <rsf type=\"{name($rsf)}\" numfacture=\"{$rsf/@NumFacture}\"/>\n" +
			"  }\n" +
			"  </numfactureerrors>\n" +
			"}\n" +
			"</result>\n" +
			"else\n" +
			"<toimplement/>)[1]");
			SednaSerializedResult pr = st.getSerializedResult();
			report = pr.next();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}
	
	public synchronized String getReport() {
		return report;
	}
	
	public void commit() throws PmsiDtoRunnableException {
		try {
			sednaConnection.commit();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}

	public void rollback() throws PmsiDtoRunnableException {
		try {
			sednaConnection.rollback();
		} catch (DriverException e) {
			if (e.getErrorCode() == 411)
				// Il n'y a pas de rollback, c'est plus une info qu'une erreur
				return;
			else
				throw new PmsiDtoRunnableException(e);
		}
	}
	
	@Override
	public void close() throws PmsiDtoRunnableException {
		try {
			sednaConnection.close();
		} catch (DriverException e) {
			throw new PmsiDtoRunnableException(e);
		}
	}

}
