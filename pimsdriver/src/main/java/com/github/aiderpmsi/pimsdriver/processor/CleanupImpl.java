package com.github.aiderpmsi.pimsdriver.processor;

import java.util.concurrent.Callable;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.actions.CleanupActions;

public class CleanupImpl implements Callable<Boolean> {

	private final Long pludId;
	
	public CleanupImpl(final Long pludId, ServletContext context) {
		this.pludId = pludId;
	}

	@Override
	public Boolean call() throws Exception {
		CleanupActions cua = new CleanupActions();
		
		// DON'T CARE IF SOMETHING GOT WRONG, WE WILL RETRY LATER
		cua.cleanup(pludId);

		return true;
	}
	
	

}
