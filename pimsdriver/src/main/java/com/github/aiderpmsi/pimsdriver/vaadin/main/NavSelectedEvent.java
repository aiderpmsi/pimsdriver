package com.github.aiderpmsi.pimsdriver.vaadin.main;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class NavSelectedEvent extends Event {
	
	public enum Type {
		factures("Facture"),
		sejours("Sejour");
		
		private String label;
		
		private Type(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
	}

	private static final long serialVersionUID = 2570810408567300226L;

	private Type type;
	
	private UploadedPmsi model;
	
	public NavSelectedEvent(Type type, UploadedPmsi model, Component source) {
		super(source);
		this.type = type;
		this.model = model;
	}

	public Type getType() {
		return type;
	}

	public UploadedPmsi getModel() {
		return model;
	}

}
