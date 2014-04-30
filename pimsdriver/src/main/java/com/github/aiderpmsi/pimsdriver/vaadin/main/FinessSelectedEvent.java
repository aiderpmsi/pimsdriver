package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class FinessSelectedEvent extends Event {

	private static final long serialVersionUID = 2570810408567300226L;

	private PmsiUploadedElementModel model;
	
	private PmsiUploadedElementModel.Status status;
	
	public FinessSelectedEvent(Component source) {
		super(source);
	}

	public PmsiUploadedElementModel getModel() {
		return model;
	}

	public void setModel(PmsiUploadedElementModel model) {
		this.model = model;
	}

	public PmsiUploadedElementModel.Status getStatus() {
		return status;
	}

	public void setStatus(PmsiUploadedElementModel.Status status) {
		this.status = status;
	}
	
}
