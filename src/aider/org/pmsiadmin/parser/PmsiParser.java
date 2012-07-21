package aider.org.pmsiadmin.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import aider.org.pmsi.dto.PmsiStreamMuxer;
import aider.org.pmsi.dto.PmsiThread;
import aider.org.pmsi.parser.PmsiRSF2009Reader;
import aider.org.pmsi.parser.PmsiRSF2012Reader;
import aider.org.pmsi.parser.PmsiRSS116Reader;
import aider.org.pmsi.parser.PmsiReader;
import aider.org.pmsi.parser.exceptions.PmsiException;
import aider.org.pmsi.parser.exceptions.PmsiReaderException;
import aider.org.pmsi.parser.exceptions.PmsiRunnableException;
import aider.org.pmsi.writer.PmsiWriter;
import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.model.xml.PmsiSednaStreamRunner;
import aider.org.pmsiadmin.model.xml.Rsf2009SednaWriter;
import aider.org.pmsiadmin.model.xml.Rsf2012SednaWriter;
import aider.org.pmsiadmin.model.xml.Rss116SednaWriter;

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
	
	/**
	 * Fonction principale du programme
	 * @param args
	 * @throws Throwable 
	 */
	public String parse(Reader re, Configuration config) throws Throwable {
		String errors = new String();
		for (FileType fileTypeEntry : listTypes) {
        	try {
        		re.reset();
        		String report = readPMSI(re, fileTypeEntry, config);
        		return report;
            } catch (PmsiReaderException e) {
            	// Erreur de reader = le reader n'est pas adapté, il faut en essayer un autre
            	errors += e.getMessage();
            } catch (PmsiRunnableException e) {
            	// Erreur du runner = la lecture a été bien réalisée, mais l'écriture n'est pas bonne :
            	// C'est une erreur, il faut arrêter
            	throw e;
            } catch (Exception e) {
            	// Pour les autres exceptions, c'est qu'il y a eu une erreur non récupérable
            	throw new RuntimeException(e);
            }
		}
		// Aucun reader n'est adapté
		throw new PmsiException(errors);
	}
	
	/**
	 * Lecture du fichier PMSI 
	 * @param options Options du programme (en particulier le fichier à insérer)
	 * @param type Type de fichier à insérer
	 * @param dtoPmsiReaderFactory Fabrique d'objets de sérialisation
	 * @return Le rapport du runner si existe

	 */
	public String readPMSI(Reader re, FileType type, Configuration config) throws Exception {
		// Reader et writer
		PmsiReader<?, ?> reader = null;
		PmsiWriter writer = null;
		PmsiStreamMuxer muxer = null;
		// Thread du lecteur de writer
		PmsiThread thread = null;
		PmsiSednaStreamRunner runner = null;
				
		try {
			// Création du transformateur de outputstream en inputstream
			muxer = new PmsiStreamMuxer();
			
			// Création de lecteur de inputstream et conenction au muxer
			runner = new PmsiSednaStreamRunner(muxer.getInputStream(), config);
			// Création du thread du lecteur de inputstream
			thread = new PmsiThread(runner);
			
			// Choix du reader et du writer et connection au muxer
			switch(type) {
				case RSS116:
					writer = new Rss116SednaWriter(muxer.getOutputStream(), runner);
					reader = new PmsiRSS116Reader(re, writer);
					break;
				case RSF2009:
					writer = new Rsf2009SednaWriter(muxer.getOutputStream(), runner);
					reader = new PmsiRSF2009Reader(re, writer);
					break;
				case RSF2012:
					writer = new Rsf2012SednaWriter(muxer.getOutputStream(), runner);
					reader = new PmsiRSF2012Reader(re, writer);
					break;
				}
			
			// lancement du lecteur de muxer
			thread.start();
	
			// Lecture du fichier par mise en route de la machine à états
			reader.run();
			
			// Fin de fichier evoyé au muxer
			muxer.eof();
	
			// Attente que le lecteur de muxer ait fini
			thread.waitEndOfProcess();

			// Récupération d'une erreur éventuelle du lecteur de muxer
			if (thread.getTerminalException() != null)
				throw thread.getTerminalException();

			// Arrivé ici, on commit, puisqu'il n'y a pas eu d'erreurs
			runner.commit();

			return runner.getReport();

		} finally {
			// Fermeture de resources
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
			if (muxer != null)
				muxer.close();
			if (runner != null) {
				runner.rollback();
				runner.close();
			}
		}
	}
}