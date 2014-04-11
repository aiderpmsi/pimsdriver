package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.io.Serializable;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.record.ORecordInternal;

/**
 * derived from vaadin abstracttransactionnalquery
 */
abstract class ODBAbstractTransactionalQuery implements Serializable {

	private static final long serialVersionUID = -6165414432982164396L;
	private ODatabaseComplex<ORecordInternal<?>> activeTx = null;

    /**
     * Starts a transaction (odb has no autocommit)
     * 
     * @throws IllegalStateException
     *             if a transaction is already open
     * @throws ODBException
     *             if a connection could not be obtained or configured
     */
    public void beginTransaction() throws UnsupportedOperationException,
            OException {
        if (isInTransaction()) {
            throw new IllegalStateException("A transaction is already active!");
        }
        activeTx = DocDbConnectionFactory.getInstance().getConnection().begin();
    }

    /**
     * Commits (if not in auto-commit mode) and releases the active connection.
     * 
     * @throws OException
     *             if not in a transaction managed by this query
     */
    public void commit() throws UnsupportedOperationException, OException {
        if (!isInTransaction()) {
            throw new OException("No active transaction");
        }
        try {
        	activeTx.commit();
        } finally {
        	activeTx.close();
        	activeTx = null;
        }
    }

    /**
     * Rolls back and releases the active connection.
     * 
     * @throws OException
     *             if not in a transaction managed by this query
     */
    public void rollback() throws UnsupportedOperationException, OException {
        if (!isInTransaction()) {
            throw new OException("No active transaction");
        }
        try {
        	activeTx.rollback();
        } finally {
        	activeTx.close();
        	activeTx = null;
        }
    }

    /**
     * Check that a transaction is active.
     * 
     * @throws OException
     *             if no active transaction
     */
    protected void ensureTransaction() throws OException {
        if (!isInTransaction()) {
            throw new OException("No active transaction!");
        }
    }

    /**
     * Returns the currently active connection, reserves and returns a new
     * connection if no active connection.
     * 
     * @return previously active or newly reserved connection
     * @throws ODBException
     */
    protected ODatabaseComplex<ORecordInternal<?>> getConnection() throws OException {
        if (activeTx != null) {
            return activeTx;
        }
        return DocDbConnectionFactory.getInstance().getConnection();
    }

    protected boolean isInTransaction() {
        return activeTx == null ? false : true;
    }

}
