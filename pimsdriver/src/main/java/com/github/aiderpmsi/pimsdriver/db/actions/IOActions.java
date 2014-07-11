package com.github.aiderpmsi.pimsdriver.db.actions;

import java.io.InputStream;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.UploadPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.UploadedPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;

public class IOActions extends DbAction {

	public IOActions(final ServletContext context) {
		super(context);
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
