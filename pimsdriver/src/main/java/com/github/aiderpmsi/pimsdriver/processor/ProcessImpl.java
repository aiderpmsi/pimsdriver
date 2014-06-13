package com.github.aiderpmsi.pimsdriver.processor;

import java.util.concurrent.Callable;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.ProcessActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;

public class ProcessImpl implements Callable<Boolean> {
	
	private UploadedPmsi element;
	
	public ProcessImpl(UploadedPmsi element) {
		this.element = element;
	}

	@Override
	public Boolean call() throws ActionException {
		ProcessActions ac = new ProcessActions();
		return ac.processPmsi(element);
	}
	
}
