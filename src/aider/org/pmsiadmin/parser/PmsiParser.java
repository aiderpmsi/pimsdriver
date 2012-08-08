package aider.org.pmsiadmin.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;

import aider.org.machinestate.MachineStateException;
import aider.org.pmsi.exceptions.PmsiParserException;
import aider.org.pmsi.exceptions.PmsiWriterException;
import aider.org.pmsi.parser.PmsiRSF2009Parser;
import aider.org.pmsi.parser.PmsiRSF2012Parser;
import aider.org.pmsi.parser.PmsiRSS116Parser;
import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.model.xml.PmsiDtoSedna;
import aider.org.pmsiadmin.model.xml.PmsiSednaXmlWriter;


/**
 * Entrée du programme permettant de lire un fichier pmsi et de le transformer en xml
 * @author delabre 
 *
 */
public class PmsiParser {

	/**
	 * Enumération permettant d'indiquer quel lecteur a réussi à réaliser la lecture du fichier
	 * pmsi (et donc de quel type de format le fichier est)
	 * @author delabre
	 */
	public enum FileType {
		RSS116, RSF2009, RSF2012;
	}
	
	/**
	 * Liste des fichiers que l'on peut lire
	 */
	public static List<FileType> listTypes = new ArrayList<PmsiParser.FileType>() {
		private static final long serialVersionUID = -4594379149065725315L;
		{
			add(FileType.RSS116);
			add(FileType.RSF2009);
			add(FileType.RSF2012);
		}
	};
	
	public String getParserLogErrors() {
		return parserLogErrors;
	}

	private String parserLogErrors = new String();
	
	/**
	 * Fonction principale du programme
	 * @param args
	 * @return renvoie true si l'insertion a été bonne, false sinon
	 * @throws DriverException 
	 * @throws PmsiWriterException 
	 * @throws MachineStateException 
	 */
	public boolean parse(Reader re, Configuration config) throws IOException, DriverException, PmsiWriterException, MachineStateException {
		// On crée une connection Sedna
		SednaConnection sednaConnection = DatabaseManager.getConnection(
				config.getSednaHost(),
				config.getSednaDb(),
				config.getSednaUser(),
				config.getSednaPwd());
		
		try {
			for (FileType fileTypeEntry : listTypes) {
	        	try {
	        		re.reset();
	        		if (readPMSI(re, fileTypeEntry, sednaConnection))
	        			return true;
	            } catch (MachineStateException e) {
	            	// Si l'erreur parente est PmsiParserException, c'est que le parseur n'a
	            	// juste pas été capable de déchiffrer le fichier, tout le reste marchait.
	            	// Il faut donc essayer avec un autre parseur
	            	if (e.getCause() instanceof PmsiParserException) {
	            		parserLogErrors += 
	            				e.getStackTrace()[0].getClassName() + " : " +
	            				e.getCause().getMessage() + "\n";
	            	} else
	            		throw e;
	            }
	        }
		} finally {
			sednaConnection.close();
		}
		
		// Aucun reader n'est adapté
		throw new IOException("Aucun parseur n'est adapté");
	}
	
	/**
	 * Lecture du fichier PMSI 
	 * @param options Options du programme (en particulier le fichier à insérer)
	 * @param type Type de fichier à insérer
	 * @param dtoPmsiReaderFactory Fabrique d'objets de sérialisation
	 * @return Le rapport du runner si existe
	 * @throws DriverException 
	 * @throws MachineStateException 
	 * @throws PmsiWriterException 

	 */
	public boolean readPMSI(Reader re, FileType type, SednaConnection sedna) throws DriverException, MachineStateException, PmsiWriterException {
		// Définitions
		aider.org.pmsi.parser.PmsiParser<?, ?> parser = null;
		PmsiSednaXmlWriter writer = null;
		PmsiDtoSedna pmsiDtoSedna = null;
		String docNumber = null;
		String date = null;

		try {
			
			try {
				// On débute une transaction
				sedna.begin();
	
				// Instanciations
				writer = new PmsiSednaXmlWriter();
				pmsiDtoSedna = new PmsiDtoSedna(sedna);
				
				// Récupération d'un nouveau numéro de fichier pmsi
				docNumber = pmsiDtoSedna.getNewPmsiDocNumber("PmsiDocIndice");
				// Récupération de la date de sedna
				date = pmsiDtoSedna.getSednaTime();
				// Activation du writer de xml dans Sedna
				writer.open(sedna, "pmsi-" + docNumber, "Pmsi", date);
				
				// Création du parseur
				switch(type) {
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
			} finally {
				cleanup(parser, writer);
			}
		} catch (DriverException e) {
			// il faut tenter un rollback
			rollback(sedna);
			throw e;
		} catch (MachineStateException e) {
			// Il faut tenter un rollback
			rollback(sedna);
			throw e;
		} catch (PmsiWriterException e){
			// Il faut tenter un rolback
			rollback(sedna);
			throw e;
		}

		// Si il n'y a pas eu d'erreurs avec le parseur, on commit et on renvoie true
		sedna.commit();
		return true;
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
}