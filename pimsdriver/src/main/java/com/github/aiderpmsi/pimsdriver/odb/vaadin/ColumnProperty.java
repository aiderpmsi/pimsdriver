package com.github.aiderpmsi.pimsdriver.odb.vaadin;

import java.util.logging.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;

/**
 * derived from vaadin ColumnProperty
 */
@SuppressWarnings("rawtypes")
final public class ColumnProperty implements Property {

	private static final long serialVersionUID = -8769575577112980160L;

	private RowItem owner;

    private String propertyId;

    private boolean readOnly;
    private boolean allowReadOnlyChange = true;
    private boolean nullable = true;

    private Object value;
    private Object changedValue;
    private Class<?> type;

    private boolean modified;

    private boolean versionColumn;
    private boolean primaryKey = false;

    /**
     * Prevent instantiation without required parameters.
     */
    protected ColumnProperty() {
    }

    /**
     * Creates a new ColumnProperty instance.
     * 
     * @param propertyId
     *            The ID of this property.
     * @param readOnly
     *            Whether this property is read-only.
     * @param allowReadOnlyChange
     *            Whether the read-only status of this property can be changed.
     * @param nullable
     *            Whether this property accepts null values.
     * @param primaryKey
     *            Whether this property corresponds to a database primary key.
     * @param value
     *            The value of this property.
     * @param type
     *            The type of this property.
     */
    public ColumnProperty(String propertyId, boolean readOnly,
            boolean allowReadOnlyChange, boolean nullable, boolean primaryKey,
            Object value, Class<?> type) {

        if (propertyId == null) {
            throw new IllegalArgumentException("Properties must be named.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Property type must be set.");
        }
        this.propertyId = propertyId;
        this.type = type;
        this.value = value;

        this.allowReadOnlyChange = allowReadOnlyChange;
        this.nullable = nullable;
        this.readOnly = readOnly;
        this.primaryKey = primaryKey;
    }

    /**
     * Returns the current value for this property. To get the previous value
     * (if one exists) for a modified property use {@link #getOldValue()}.
     * 
     * @return
     */
    @Override
    public Object getValue() {
        if (isModified()) {
            return changedValue;
        }
        return value;
    }

    /**
     * Returns the original non-modified value of this property if it has been
     * modified.
     * 
     * @return The original value if <code>isModified()</code> is true,
     *         <code>getValue()</code> otherwise.
     */
    public Object getOldValue() {
        return value;
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (newValue == null && !nullable) {
            throw new NotNullableException(
                    "Null values are not allowed for this property.");
        }
        if (readOnly) {
            throw new ReadOnlyException(
                    "Cannot set value for read-only property.");
        }

        if (newValue != null) {

        	if (!getType().isAssignableFrom(newValue.getClass())) {
        		throw new IllegalArgumentException(
        				"Illegal value type for ColumnProperty");
        	}

        	/*
        	 * If the value to be set is the same that has already been set, do
             * not set it again.
             */
            if (isValueAlreadySet(newValue)) {
                return;
            }
        }

        /* Set the new value and notify container of the change. */
        changedValue = newValue;
        modified = true;
        owner.getContainer().itemChangeNotification(owner);
    }

    private boolean isValueAlreadySet(Object newValue) {
    	Object referenceValue = isModified() ? changedValue : value;

        return (isNullable() && newValue == null && referenceValue == null)
                || newValue.equals(referenceValue);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns whether the read-only status of this property can be changed
     * using {@link #setReadOnly(boolean)}.
     * <p>
     * Used to prevent setting to read/write mode a property that is not allowed
     * to be written by the underlying database. Also used for values like
     * VERSION and AUTO_INCREMENT fields that might be set to read-only by the
     * container but the database still allows writes.
     * 
     * @return true if the read-only status can be changed, false otherwise.
     */
    public boolean isReadOnlyChangeAllowed() {
        return allowReadOnlyChange;
    }

    @Override
    public void setReadOnly(boolean newStatus) {
        if (allowReadOnlyChange) {
            readOnly = newStatus;
        }
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getPropertyId() {
        return propertyId;
    }

    @SuppressWarnings("unused")
	private static Logger getLogger() {
        return Logger.getLogger(ColumnProperty.class.getName());
    }

    public void setOwner(RowItem owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner can not be set to null.");
        }
        if (this.owner != null) {
            throw new IllegalStateException(
                    "ColumnProperties can only be bound once.");
        }
        this.owner = owner;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isVersionColumn() {
        return versionColumn;
    }

    public void setVersionColumn(boolean versionColumn) {
        this.versionColumn = versionColumn;
    }

    public boolean isNullable() {
        return nullable;
    }

    /**
     * Return whether the value of this property should be persisted to the
     * database.
     * 
     * @return true if the value should be written to the database, false
     *         otherwise.
     */
    public boolean isPersistent() {
        if (isVersionColumn()) {
            return false;
        } else if (isReadOnlyChangeAllowed() && !isReadOnly()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether or not this property is used as a row identifier.
     * 
     * @return true if the property is a row identifier, false otherwise.
     */
    public boolean isRowIdentifier() {
        return isPrimaryKey() || isVersionColumn();
    }

    public void commit() {
        if (isModified()) {
            modified = false;
            value = changedValue;
        }
    }
    
    /**
     * An exception that signals that a <code>null</code> value was passed to
     * the <code>setValue</code> method, but the value of this property can not
     * be set to <code>null</code>.
     */
    public class NotNullableException extends RuntimeException {

    	private static final long serialVersionUID = -2064686956145335829L;

    	/**
    	 * Constructs a new <code>NotNullableException</code> without a detail
    	 * message.
    	 */
    	public NotNullableException() {
    	}

    	/**
    	 * Constructs a new <code>NotNullableException</code> with the specified
    	 * detail message.
    	 * 
    	 * @param msg
    	 *            the detail message
    	 */
    	public NotNullableException(String msg) {
    		super(msg);
    	}

    	/**
    	 * Constructs a new <code>NotNullableException</code> from another
    	 * exception.
    	 * 
    	 * @param cause
    	 *            The cause of the failure
    	 */
    	public NotNullableException(Throwable cause) {
    		super(cause);
    	}
    }
}
