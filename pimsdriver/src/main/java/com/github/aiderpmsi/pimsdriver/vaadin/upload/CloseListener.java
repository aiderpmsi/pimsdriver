package com.github.aiderpmsi.pimsdriver.vaadin.upload;

import com.github.aiderpmsi.pimsdriver.vaadin.utils.FileUploader;
import com.vaadin.ui.Window.CloseEvent;

public class CloseListener implements com.vaadin.ui.Window.CloseListener {

	private static final long serialVersionUID = -983340052018092722L;

	private FileUploader rsf, rss;
	
	public CloseListener(FileUploader rsf, FileUploader rss) {
		this.rsf = rsf;
		this.rss = rss;
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		// DELETES THE UPLOADED FILES
		rss.release();
		rsf.release();
	}

}
