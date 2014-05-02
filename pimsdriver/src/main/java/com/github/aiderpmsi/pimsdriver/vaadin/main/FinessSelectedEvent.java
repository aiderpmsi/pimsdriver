package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class FinessSelectedEvent extends Event {

	private static final long serialVersionUID = 2570810408567300226L;

	private UploadedPmsi model;
	
	private UploadedPmsi.Status status;
	
	public FinessSelectedEvent(Component source) {
		super(source);
	}

	public UploadedPmsi getModel() {
		return model;
	}

	public void setModel(UploadedPmsi model) {
		this.model = model;
	}

	public UploadedPmsi.Status getStatus() {
		return status;
	}

	public void setStatus(UploadedPmsi.Status status) {
		this.status = status;
	}
	
}
