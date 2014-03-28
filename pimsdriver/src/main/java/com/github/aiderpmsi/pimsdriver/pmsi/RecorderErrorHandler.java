package com.github.aiderpmsi.pimsdriver.pmsi;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RecorderErrorHandler implements ErrorHandler {

	/**
	 * List of recovered errors
	 */
	private List<SAXParseException> errors = new LinkedList<>();
	
	/**
	 * List of warnings
	 */
	private List<SAXParseException> warnings = new LinkedList<>();
	
	private SAXParseException fatalError = null;
	
	@Override
	public void error(SAXParseException arg0) throws SAXException {
		// RECOVERABLE ERROR, JUST SAVE
		errors.add(arg0);
	}

	@Override
	public void fatalError(SAXParseException arg0) throws SAXException {
		// UNRECOVERABLE ERROR, RETHROW
		fatalError = arg0;
		throw new SAXException(fatalError);
	}

	@Override
	public void warning(SAXParseException arg0) throws SAXException {
		// INFORMATION, JUST SAVE
		warnings.add(arg0);
	}

	public List<SAXParseException> getErrors() {
		return errors;
	}

	public List<SAXParseException> getWarnings() {
		return warnings;
	}

	public SAXParseException getFatalError() {
		return fatalError;
	}

	
}
