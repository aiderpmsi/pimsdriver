package com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.github.aiderpmsi.pims.parser.utils.Utils.ErrorHandler;

public class SimpleErrorHandler implements ErrorHandler {

	/**
	 * List of encountered errors
	 */
	private List<Error> errors = new LinkedList<>();
	
	@Override
	public void error(String msg, long line) throws IOException {
		errors.add(new Error(msg, line));
	}
	
	public Collection<Error> getErrors() {
		return errors;
	}

	public class Error {
		public String msg;
		public long line;
		public Error(String msg, long line) {
			this.msg = msg;
			this.line = line;
		}
	}
	
}
