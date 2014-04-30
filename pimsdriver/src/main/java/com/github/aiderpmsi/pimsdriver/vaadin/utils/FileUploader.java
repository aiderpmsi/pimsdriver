package com.github.aiderpmsi.pimsdriver.vaadin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Window;

public class FileUploader implements Receiver {

	private static final long serialVersionUID = 5675725310161340636L;
    private String filename = null;
    private String mimeType = null;
    private String resourceName;
    private TextField feedback = null;
    
	public FileUploader(String type, Window window) {
		// DEFINES THE NAME OF THE DOWNLOADED RESOURCE
		resourceName = "/tmp/uploads/" + type + "_" + Integer.toHexString(window.hashCode());
    	// REMOVES IT FROM FILESYSTEM IF ALREADY EXISTS
    	new File(resourceName).delete();
    }
    
	@Override
    public OutputStream receiveUpload(String filename,
                                      String mimeType) {
    	// REINIT FILENAME AND MIMETYPE
    	this.filename = null;
        this.mimeType = null;

    	// UPLOADSTREAM
    	OutputStream fos = null;

       	try {
       		// CREATE FILE
       		File file = new File(resourceName);
       		file.getParentFile().mkdirs();
       		fos = new FileOutputStream(file);
       		// SETS FILENAME AND MIMETYPE
       		this.filename = filename;
       		this.mimeType = mimeType;
       		// ADDS FILENAME TO FEEDBACK IF NOT ONE FEEDBACK ELEMENT HAS BEEN DEFINED
       		if (feedback != null)
       			feedback.setValue(filename);
       	} catch (final FileNotFoundException e) {
       		new Notification("Could not open file<br/>",
       				e.getMessage(),
       				Notification.Type.ERROR_MESSAGE)
       		.show(Page.getCurrent());
        }
        
        // RETURNS THE STREAM WE HAVE TO WRITE INTO
        return fos;
    }
	
	public InputStream getFile() {
    	// RETURNED INPUTSTREAM
    	InputStream fis = null;
    	// IF FILENAME IS NULL, WE HAVE NO STREAM, ELSE WE HAVE ONE STREAM
    	if (filename != null) {
    		try {
    			File file = new File(resourceName);
    			fis = new FileInputStream(file);
    		} catch (IOException e) {
    			// DO NOTHING, RETURN NULL VALUE
    		}
    	}
    	return fis;
    }
	
	/**
	 * Deletes the corresponding file (use before destroying the object)
	 */
	public void release() {
    	new File(resourceName).delete();
	}
    
    public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public TextField getFeedback() {
		return feedback;
	}

	public void setFeedback(TextField feedback) {
		this.feedback = feedback;
	}
   
}