package com.github.aiderpmsi.pimsdriver.dao;

import javax.transaction.TransactionalException;

public class TransactionException extends TransactionalException {

	private static final long serialVersionUID = -7555000872265630868L;

	public TransactionException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
