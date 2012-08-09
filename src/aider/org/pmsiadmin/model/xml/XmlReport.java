package aider.org.pmsiadmin.model.xml;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public class XmlReport {

	private Integer countIdentityErrors = 0,
			countFinessErrors = 0,
			countNumFactureErrors = 0;
	
	private String report;
		
	public XmlReport(String inputString) throws XMLStreamException {
		// Définition du report
		this.report = inputString;
		// Création du reader stax
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlsr = xmlif.createXMLStreamReader(new StringReader(inputString));
		// Lecture du xml
		readMain(xmlsr);
	}
	
	private void readMain(XMLStreamReader xmlsr) throws XMLStreamException {
		// Type d'évenement
		int eventType;
		
		// Boucle de lecture du xml
		while (xmlsr.hasNext()) {
			eventType = xmlsr.next();
			
			// Début d'élément
			if (eventType == XMLEvent.START_ELEMENT) {			
				// Choix de la méthode à effectuer selon le nom de la balise
				if (xmlsr.getName().toString().equals("parent"))
					readParent(xmlsr);
				else if (xmlsr.getName().toString().equals("identityerrors"))
					countIdentityErrors = readNumErrors(xmlsr);
				else if (xmlsr.getName().toString().equals("finesserrors"))
					countFinessErrors = readNumErrors(xmlsr);
				else if (xmlsr.getName().toString().equals("numfactureerrors"))
					countNumFactureErrors = readNumErrors(xmlsr);
			}
		}
	}
	
	private void readParent(XMLStreamReader xmlsr) throws XMLStreamException {
		// Type d'évenement
		int eventType;
		// Compteur d'entrées dans un élément
		int nbStart = 0;
		
		// Boucle de lecture du xml
		while (xmlsr.hasNext()) {
			eventType = xmlsr.next();
			
			if (eventType == XMLEvent.START_ELEMENT) {
				// Prise en compte de l'entrée dans un élément
				nbStart = nbStart + 1;
			} else if (eventType == XMLEvent.END_ELEMENT) {
				nbStart = nbStart - 1;
				// Si on est sorti de plus d'éléments qu'on est rentré, il faut
				// retourner à la boucle main
				if (nbStart < 0)
					return;
			}
		}
	}
	
	private Integer readNumErrors(XMLStreamReader xmlsr) throws XMLStreamException {
		Integer countNb = null;
		// Compteur d'entrées dans un élément
		int nbStart = 0;
		// Type d'évemenement
		int eventType;
		
		// On est déjà dans l'élément, il faut récupérer l'attribut 'count'
		int attCount = xmlsr.getAttributeCount();
		for (int i = 0 ; i < attCount ; i++) {
			if (xmlsr.getAttributeLocalName(i).equals("count"))
				countNb =  new Integer(xmlsr.getAttributeValue(i));
		}
		
		// Maintenant il faut sortir de cet élément
		while (xmlsr.hasNext()) {
			eventType = xmlsr.next();
			
			if (eventType == XMLEvent.START_ELEMENT) {
				nbStart = nbStart + 1;
			} else if (eventType == XMLEvent.END_ELEMENT) {
				nbStart = nbStart - 1;
				if (nbStart < 0)
					break;
			}
		}
		
		return countNb;
	}

	public Integer getCountIdentityErrors() {
		return countIdentityErrors;
	}

	public Integer getCountFinessErrors() {
		return countFinessErrors;
	}

	public Integer getCountNumFactureErrors() {
		return countNumFactureErrors;
	}

	public String getReport() {
		return report;
	}

}
