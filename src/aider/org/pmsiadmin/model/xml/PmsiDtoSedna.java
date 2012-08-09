package aider.org.pmsiadmin.model.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import aider.org.machinestate.MachineStateException;
import aider.org.pmsi.exceptions.PmsiParserException;
import aider.org.pmsi.exceptions.PmsiWriterException;
import aider.org.pmsi.parser.PmsiRSF2009Parser;
import aider.org.pmsi.parser.PmsiRSF2012Parser;
import aider.org.pmsi.parser.PmsiRSS116Parser;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

public class PmsiDtoSedna {
	
	/**
	 * Enumération permettant de lister les types de fichiers pmsi qu'on peut lire
	 * @author delabre
	 */
	public enum FileType {
		RSS116, RSF2009, RSF2012;
	}
	
	/**
	 * Classe permettant de renvoyer le résultat d'un stockage de Pmsi :
	 * <ul>
	 *  <li>Etat de réussite ou non</li>
	 *  <li>Liste des erreurs des parseurs</li>
	 * </ul>
	 */
	public class StoreResult {
		public Boolean stateSuccess = false;
		public List<PmsiParserException> parseErrors = new ArrayList<PmsiParserException>();
	}
	
	private SednaConnection sednaConnection;
	
	public PmsiDtoSedna(SednaConnection sednaConnection) {
		this.sednaConnection = sednaConnection;
	}
	
	public StoreResult storePmsi(Reader re, String docName, String collectionName, String date) throws IOException, PmsiWriterException, DriverException, MachineStateException {
		StoreResult storeResult = new StoreResult();
		
		for (FileType fileType : FileType.values()) {
        	try {
        		re.reset();
        		storePmsi(re, fileType, docName, collectionName, date);
        		storeResult.stateSuccess = true;
            } catch (MachineStateException e) {
            	// Si l'erreur parente est PmsiParserException, c'est que le parseur n'a
            	// juste pas été capable de déchiffrer le fichier, tout le reste marchait.
            	// Il faut donc essayer avec un autre parseur
            	if (e.getCause() instanceof PmsiParserException) {
            		storeResult.parseErrors.add((PmsiParserException) e.getCause());
            	} else
            		throw e;
            }
        }
		
		return storeResult;
	}
	
	private void storePmsi(Reader re, FileType fileType, String docName, String collectionName, String date) throws DriverException, MachineStateException, PmsiWriterException  {
		// Définitions
		aider.org.pmsi.parser.PmsiParser<?, ?> parser = null;
		PmsiSednaXmlWriter writer = null;
	
		try {	
			// Instanciations
			writer = new PmsiSednaXmlWriter();
			
			// Activation du writer de xml dans Sedna
			writer.open(sednaConnection, docName, collectionName, date);
			
			// Création du parseur
			switch(fileType) {
				case RSS116:
					parser = new PmsiRSS116Parser(re, writer);
					break;
				case RSF2009:
					parser = new PmsiRSF2009Parser(re, writer);
					break;
				case RSF2012:
					parser = new PmsiRSF2012Parser(re, writer);
					break;
			}
			
			// lancement du parseur
			parser.call();
		} catch (MachineStateException e) {
			// En cas de cleanup après une exception de MachineState, le parseur a échoué,
			// ses erreurs ont priorité sur les autres
			try {
				cleanup(parser, writer);
			} catch (Exception e2) {}
			// Il faut tenter un rollback, puis recommencer la transaction
			rollback(sednaConnection);
			sednaConnection.begin();
			throw e;
		}
	
		// Si il n'y a pas eu d'erreurs avec le parseur, on nettoie, et on laisse en l'état
		cleanup(parser, writer);
	}

	private void cleanup(
			aider.org.pmsi.parser.PmsiParser<?, ?> parser,
			PmsiSednaXmlWriter writer) throws PmsiWriterException, MachineStateException {
		if (writer != null) {
			writer.close();
			writer = null;
		}
		if (parser != null) {
			parser.close();
			parser = null;
		}
	}
	
	private void rollback(SednaConnection sednaConnection) throws DriverException {
		try {
			sednaConnection.rollback();
		} catch (DriverException e) {
			if (e.getErrorCode() == 411) {
				// Pas une erreur, une info plutôt qui dit qu'aucune transaction
				// n'était ouverte
			} else {
				throw e;
			}
		}

	}

	
	/**
	 * Récupère dans Sedna un nouveau numéro unique pour un compteur
	 * @throws DriverException 
	 * @throws DtoPmsiException 
	 */
	public String getNewPmsiDocNumber(String docCounter) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("update \n" +
				"replace $l in fn:doc(\"" + docCounter + "\", \"Pmsi\")/indice \n" +
				"with <indice>{$l/text() + 1}</indice>");

		st = sednaConnection.createStatement();
		st.execute("fn:doc(\"" + docCounter + "\", \"Pmsi\")/indice/text()");
		SednaSerializedResult pr = st.getSerializedResult();

		return pr.next();
	}
	
	/**
	 * Récupère dans Sedna un le numéro de compeur actuel
	 * @throws DriverException 
	 * @throws DtoPmsiException 
	 */
	public String getPmsiDocNumber(String docCounter) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("fn:doc(\"" + docCounter + "\", \"Pmsi\")/indice/text()");
		SednaSerializedResult pr = st.getSerializedResult();

		return pr.next();
	}

	/**
	 * @throws DriverException 
	 * Récupère dans sedna l'heure de la db
	 * @throws  
	 */
	public String getSednaTime() throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("current-dateTime()");
		SednaSerializedResult pr = st.getSerializedResult();
		return pr.next();
	}

	/**
	 * Retourne le rapport d'insertion
	 * @return
	 * @throws DriverException 
	 * @throws PmsiDtoException
	 */
	public String getReport(String docName, String collectionName) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("(for $i in fn:doc(\"" + docName + "\", \"" + collectionName + "\")/(*[1])/(*[1])\n" +
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
		return pr.next();
	}

}
