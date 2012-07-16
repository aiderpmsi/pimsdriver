package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.SednaConnection;
import aider.org.pmsi.dto.DtoPmsiException;
import aider.org.pmsi.parser.PmsiRSF2009Reader;
import aider.org.pmsi.parser.linestypes.PmsiLineType;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009Header;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009a;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009b;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009c;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009h;
import aider.org.pmsi.parser.linestypes.PmsiRsf2009m;

/**
 * Objet de transfert de données pour un {@link PmsiRSF2009Reader}
 * @author delabre
 *
 */
public class DtoXmlRsf2009 extends DtoPmsiXmlImpl {

	/**
	 * Construction de la connexion à la base de données à partir des configurations
	 * données
	 * @throws DtoPmsiException 
	 */
	public DtoXmlRsf2009(SednaConnection connection) throws DtoPmsiException {
		super(connection);
	}
	
	/**
	 * Ajoute des données liées à une ligne pmsi
	 * @param lineType ligne avec les données à insérer
	 * @throws DtoPmsiException 
	 */
	public void writeLineElement(PmsiLineType lineType) throws DtoPmsiException {
		// Header
		if (lineType instanceof PmsiRsf2009Header) {
			// Ecriture de la ligne header sans la fermer (va contenir les rsf)
			super.writeLineElement(lineType);
		}
		
		// Ligne RSFA
		else if (lineType instanceof PmsiRsf2009a) {
			// Si un rsfa est ouvert, il faut d'abord le fermer
			if (getLastLine() instanceof PmsiRsf2009a) {
				super.writeEndElement();
			}
			// ouverture du nouveau rsfa
			super.writeLineElement(lineType);
		}
		
		// Autres lignes
		else if (lineType instanceof PmsiRsf2009b || lineType instanceof PmsiRsf2009c ||
				lineType instanceof PmsiRsf2009h || lineType instanceof PmsiRsf2009m) {
			// Ouverture de la ligne
			super.writeLineElement(lineType);
			// fermeture de la ligne
			super.writeEndElement();
		}
	}
}
