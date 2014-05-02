package com.github.aiderpmsi.pimsdriver.processor;

import java.util.concurrent.Callable;

import com.github.aiderpmsi.pimsdriver.dao.ImportPmsiDTO;

public class CleanupImpl implements Callable<Boolean> {

	private Long pludId;
	
	public CleanupImpl(Long pludId) {
		this.pludId = pludId;
	}

	@Override
	public Boolean call() throws Exception {
		(new ImportPmsiDTO()).cleanup(pludId);
		return true;
	}
	
	

}
