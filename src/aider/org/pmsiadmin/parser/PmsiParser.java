package aider.org.pmsiadmin.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import aider.org.pmsi.dto.PmsiPipedWriterFactory;
import aider.org.pmsi.parser.PmsiRSF2009Reader;
import aider.org.pmsi.parser.PmsiRSF2012Reader;
import aider.org.pmsi.parser.PmsiRSS116Reader;
import aider.org.pmsi.parser.PmsiReader;
import aider.org.pmsi.parser.exceptions.PmsiIOException;
import aider.org.pmsi.parser.exceptions.PmsiPipedIOException;
import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.model.xml.PmsiSednaPipedReaderFactory;

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
	 * Chaine de caractère stockant les erreurs des
	 * lecteurs de fichiers de PMSI
	 */
	public String pmsiErrors = "";
	
	/**
	 * Fonction principale du programme
	 * @param args
	 * @throws Throwable 
	 */
	public FileType parse(Reader re, Configuration config) throws Throwable  {
		// Création de la facrique de transfert du pmsi
		PmsiPipedWriterFactory pmsiPipedWriterFactory = new PmsiPipedWriterFactory(
				new PmsiSednaPipedReaderFactory(config));
        // On essaye de lire le fichier pmsi donné avec tous les lecteurs dont on dispose,
        // Le premier qui réussit est considéré comme le bon
        for (FileType fileTypeEntry : listTypes) {
        	try {
        		if (readPMSI(re, fileTypeEntry, pmsiPipedWriterFactory) == true) {
        			return fileTypeEntry;
        		}
            } catch (Throwable e) {
            	if (e instanceof PmsiPipedIOException || e instanceof PmsiIOException) {
            		pmsiErrors += (e.getMessage() == null ? "" : e.getMessage());
            	} else
            		throw e;
            }
        }
        return null;
	}
	
	public String getPmsiErrors() {
		return pmsiErrors;
	}
	
	/**
	 * Lecture du fichier PMSI 
	 * @param options Options du programme (en particulier le fichier à insérer)
	 * @param type Type de fichier à insérer
	 * @param dtoPmsiReaderFactory Fabrique d'objets de sérialisation
	 * @return true si le fichier a pu être inséré, false sinon
	 * @throws Exception 
	 */
	public boolean readPMSI(Reader re, FileType type, PmsiPipedWriterFactory pmsiPipedWriterFactory) throws Exception {
		PmsiReader<?, ?> reader = null;
		re.reset();
		try {
			// Choix du reader
			switch(type) {
				case RSS116:
					reader = new PmsiRSS116Reader(re, pmsiPipedWriterFactory);
					break;
				case RSF2009:
					reader = new PmsiRSF2009Reader(re, pmsiPipedWriterFactory);
					break;
				case RSF2012:
					reader = new PmsiRSF2012Reader(re, pmsiPipedWriterFactory);
					break;
				}
	
			// Lecture du fichier par mise en route de la machine à états
	        reader.run();
		} catch (Exception e) {
			// Si on arrive ici, c'est qu'il existe une erreur qui interdit la transformation
			// du pmsi en xml
			// Les 2 seules erreurs qui peuvent arriver ici sont :
			// - PmsiIOException (Lecture impossible)
			// - PmsiPipedIOException (ecriture impossible)
			// Ce sont les erreurs les plus importantes, peu importe dans ce cas si la
			// fermeture du reader échoue
			try {
				if (reader != null)
					reader.close();
			} catch (PmsiPipedIOException ignore) {}
			throw e;
		}

        // Arrivé ici, le fichier a pu être lu, on ferme le reader
		reader.close();
			
        // Arrivé ici, le fichier a pu être lu, on retourne true
        return true;
	}
}
