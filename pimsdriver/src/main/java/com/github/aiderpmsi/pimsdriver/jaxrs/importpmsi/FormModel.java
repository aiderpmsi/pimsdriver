package com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi;

import java.util.Map;

public class FormModel {

	private Map<String, String> errorsModel;

	public Map<String, String> getErrorsModel() {
		return errorsModel;
	}

	public void setErrorsModel(Map<String, String> errorsModel) {
		this.errorsModel = errorsModel;
	}

}
