package com.github.aiderpmsi.pimsdriver.processor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class ProcessTask implements Callable<Boolean> {

	static final Logger log = Logger.getLogger(ProcessTask.class.toString());

	@Override
	public Boolean call() throws Exception {
		// LANCEMENT DE LA RECHERCHE DE PMSI A TRAITER TOUTES LES MINUTES
		ExecutorService execute = Executors.newSingleThreadExecutor();
		ODatabaseDocumentTx tx = null;
		while (true) {
			// RECUPERATION DES PMSI A TRAITER :
			OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>("select @RID, rsf, rss from PmsiUpload where processed='pending'");
			List<ODocument> results = null;
			try {
				tx = DataSourceSingleton.getInstance().getConnection();
				tx.begin();
				results = tx.query(oquery);
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
					futureResult.get();
				} catch (InterruptedException e) {
					break;
				} catch (ExecutionException e) {
					log.warning("Erreur dans ProcessImpl : " + e.getMessage());
				}
				
				if (Thread.interrupted())
					break;
			}
			
			// ATTENTE DE 30 SECONDES
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				break;
			}
			
			// IF THIS THREAD HAS BEEN INTERRUPTED, GO AWAY
			if (Thread.interrupted())
				break;
		}	
		
		return true;
	}

}
