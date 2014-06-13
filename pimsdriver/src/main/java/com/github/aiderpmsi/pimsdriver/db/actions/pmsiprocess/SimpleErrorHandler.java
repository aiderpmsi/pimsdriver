package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler{

	private List<SAXParseException> warnings = new ArrayList<SAXParseException>();
	
	private List<SAXParseException> errors = new ArrayList<SAXParseException>();
	
	private SAXParseException fatalError = null;

	@Override
	public void error(SAXParseException arg0) throws SAXException {
		// RECOVERABLE ERROR
		errors.add(arg0);
	}

	@Override
	public void fatalError(SAXParseException arg0) throws SAXException {
		// FATAL ERROR
		fatalError = arg0;

	}

	@Override
	public void warning(SAXParseException arg0) throws SAXException {
		// RECOVERABLE EXCEPTION
		warnings.add(arg0);
	}

	public List<SAXParseException> getWarnings() {
		return warnings;
	}

	public List<SAXParseException> getErrors() {
		return errors;
	}

	public SAXParseException getFatalError() {
		return fatalError;
	}

}
