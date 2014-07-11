package com.github.aiderpmsi.pimsdriver.db.actions;

import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.CleanupDTO;

public class CleanupActions extends DbAction {
	
	public CleanupActions(final ServletContext context) {
		super(context);
	}

	public List<Long> getToCleanup() throws ActionException {
		return execute(CleanupDTO.class,
				(dto) -> dto.readList());
	}

	public Boolean cleanup(final Long cleanupId) throws ActionException {
		return execute(CleanupDTO.class,
				(dto) -> dto.delete(cleanupId));
	}
	
}