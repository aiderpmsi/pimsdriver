package com.github.aiderpmsi.pimsdriver.db.actions;

import java.io.InputStream;
import com.github.aiderpmsi.pimsdriver.dto.UploadPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.server.VaadinRequest;

public class IOActions extends DbAction {

	public IOActions(VaadinRequest request) {
		super(request);
	}

	public Long uploadPmsi(final UploadPmsi model, final InputStream rsf, final InputStream rss) throws ActionException {
		return execute(UploadPmsiDTO.class,
				(dto) -> dto.create(model, rsf, rss));
	}

	public Boolean deletePmsi(UploadedPmsi model) throws ActionException {
		return execute(UploadedPmsiDTO.class,
				(UploadedPmsiDTO dto) -> dto.delete(model));
	}

}
