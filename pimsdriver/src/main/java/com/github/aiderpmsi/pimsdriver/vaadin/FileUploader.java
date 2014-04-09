package com.github.aiderpmsi.pimsdriver.vaadin;

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
	private String type;
    private String filename = null;
    private String mimeType = null;
    private TextField feedback = null;
    private Window window;
    
	public FileUploader(String type, Window window) {
    	// SELECTS THE TYPE OF FILE
    	this.type = type;
    	this.window = window;
    	// REMOVES IT FROM FILESYSTEM IF ALREADY EXISTS
    	(new File("/tmp/uploads/" + type + "/" + Integer.toHexString(window.hashCode()))).delete();
    }
    
    public OutputStream receiveUpload(String filename,
                                      String mimeType) {
    	// REINIT FILENAME AND MIMETYPE
    	this.filename = null;
        this.mimeType = null;
        // UPLOADSTREAM
        FileOutputStream fos = null;
        try {
            // CREATE FILE
            File file = new File("/tmp/uploads/" + type + "/" + Integer.toHexString(window.hashCode()));
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            // SETS FILENAME AND MIMETYPE
            this.filename = (filename == null ? "" : filename);
            this.mimeType = (mimeType == null ? "" : mimeType);
            // ADDS FILENAME TO FEEDBACK IF NOT NULL
            if (feedback != null)
            	feedback.setValue(filename);
        } catch (final FileNotFoundException e) {
            new Notification("Could not open file<br/>",
                             e.getMessage(),
                             Notification.Type.ERROR_MESSAGE)
                .show(Page.getCurrent());
            return null;
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
    			File file = new File("/tmp/uploads/" + type + "/" + Integer.toHexString(window.hashCode()));
    			fis = new FileInputStream(file);
    		} catch (IOException e) {
    			// DO NOTHING, RETURN NULL VALUE
    		}
    	}
    	return fis;
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