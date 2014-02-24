package com.github.aiderpmsi.pimsdriver.processor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class ProcessTask implements Callable<Boolean> {


	@Override
	public Boolean call() throws Exception {
		// AT STARTING, TRANSFORM THE PENDING ACTION TO WAITING ACTION
		// (THEY WERE ABORTED)
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>("update PmsiUpload set processed = 'waiting' WHERE processed='pending'");
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getConnection();
			tx.begin();
			tx.command(oquery).execute();
			tx.commit();
		} finally {
			if (tx != null) {
				tx.close();
				tx = null;
			}
		}

		// LANCEMENT DE LA RECHERCHE DE PMSI A TRAITER TOUTES LES MINUTES
		ExecutorService execute = Executors.newSingleThreadExecutor();
		while (true) {
			// RECUPERATION DES PMSI A TRAITER :
			oquery = new OSQLSynchQuery<ODocument>("select rsf, rss from PmsiUpload where processed='pending'");
			List<ODocument> results = null;
			try {
				tx = DocDbConnectionFactory.getConnection();
				tx.begin();
				results = tx.command(oquery).execute();
				tx.commit();
			} finally {
				if (tx != null) {
					tx.close();
					tx = null;
				}
			}

			// TRAITEMENT DES PMSI UN PAR UN :
			for (ODocument result : results) {
				ProcessImpl processImpl = new ProcessImpl(result);
				Future<Boolean> futureResult = execute.submit(processImpl);
				// WAIT THE RESULT OF THE COMPUTATION
				try {
					Boolean realResult = futureResult.get();
				} catch (InterruptedException e) {
					break;
				}
			}
			
			// IF THIS THREAD HAS BEEN INTERRUPTED, GO AWAY
			if (Thread.interrupted())
				break;
		}		
		
		return true;
	}

}
