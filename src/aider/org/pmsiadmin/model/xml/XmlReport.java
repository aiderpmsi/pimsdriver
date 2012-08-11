package aider.org.pmsiadmin.model.xml;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

public class XmlReport {

	private static XPath xpath;

	private final static String countIdentityXPath = "/report/identityerrors/@count";
	private final static String countFinessXPath = "/report/finesserrors/@count";
	private final static String countNumFactureXPath = "/report/numfactureerrors/@count";
	
	private static XPathExpression countIdentityXPathE;
	private static XPathExpression countFinessXPathE;
	private static XPathExpression countNumFactureXPathE;
	
	private static Boolean initialized = false;

	private Integer
		countIdentityErrors = null,
		countFinessErrors = null,
		countNumFactureErrors = null;
	
	private String report;
		
	public XmlReport(String inputString) throws XMLStreamException, XPathExpressionException {
		// Définition du report
		this.report = inputString;
		
		// Si besoin, initialisation de la lecture de XPath
		synchronized(initialized) {
			if (initialized == false) {
				XPathFactory xPathFactory = XPathFactory.newInstance();
				xpath = xPathFactory.newXPath();
				countIdentityXPathE = xpath.compile(countIdentityXPath);
				countFinessXPathE = xpath.compile(countFinessXPath);
				countNumFactureXPathE = xpath.compile(countNumFactureXPath);
				initialized = true;
			}
		}
	}

	public Integer getCountIdentityErrors() {
		// Si le nombre a déjà été parsé,on le renvoie
		if (countIdentityErrors != null)
			return countIdentityErrors;
		
		countIdentityErrors = getIntegerFromXPathExpression(countIdentityXPathE);
		if (countIdentityErrors == null)
			countIdentityErrors = 0;
		
		return countIdentityErrors;
	}

	public Integer getCountFinessErrors() {
		// Si le nombre a déjà été parsé,on le renvoie
		if (countFinessErrors != null)
			return countFinessErrors;
		
		countFinessErrors = getIntegerFromXPathExpression(countFinessXPathE);
		if (countFinessErrors == null)
			countFinessErrors = 0;
		
		return countFinessErrors;
	}

	public Integer getCountNumFactureErrors() {
		// Si le nombre a déjà été parsé,on le renvoie
		if (countNumFactureErrors != null)
			return countNumFactureErrors;
		
		countNumFactureErrors = getIntegerFromXPathExpression(countNumFactureXPathE);
		if (countNumFactureErrors == null)
			countNumFactureErrors = 0;
		
		return countNumFactureErrors;
	}
	
	private Integer getIntegerFromXPathExpression(XPathExpression xpe) {
		InputSource inputSource = new InputSource(new ByteArrayInputStream(report.getBytes()));
		
		try {
			String result = (String) xpe.evaluate(inputSource, XPathConstants.STRING);
			return Integer.parseInt(result);
		} catch (NumberFormatException e) {
			return null;
		} catch (XPathExpressionException e) {
			return null;
		}
	}

	public String getReport() {
		return report;
	}

}
