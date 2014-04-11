package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.io.Serializable;

/**
 * Derived from vaadin Reference
 */
class Reference implements Serializable {

    /**
	 * Generated serial id
	 */
	private static final long serialVersionUID = -4020514284546126694L;

	/**
     * The SQLContainer that this reference points to.
     */
    private ODBContainer referencedContainer;

    /**
     * The column ID/name in the referencing SQLContainer that contains the key
     * used for the reference.
     */
    private String referencingColumn;

    /**
     * The column ID/name in the referenced SQLContainer that contains the key
     * used for the reference.
     */
    private String referencedColumn;

    /**
     * Constructs a new reference to be used within the SQLContainer to
     * reference another SQLContainer.
     */
    Reference(ODBContainer referencedContainer, String referencingColumn,
            String referencedColumn) {
        this.referencedContainer = referencedContainer;
        this.referencingColumn = referencingColumn;
        this.referencedColumn = referencedColumn;
    }

    ODBContainer getReferencedContainer() {
        return referencedContainer;
    }

    String getReferencingColumn() {
        return referencingColumn;
    }

    String getReferencedColumn() {
        return referencedColumn;
    }
}
