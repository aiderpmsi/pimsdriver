package com.github.aiderpmsi.pimsdriver.processor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.github.aiderpmsi.pimsdriver.dao.ImportPmsiDTO;
import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDTO;
import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;

public class ProcessTask implements Callable<Boolean> {

	static final Logger log = Logger.getLogger(ProcessTask.class.toString());

	@Override
	public Boolean call() throws Exception {
		// LANCEMENT DE LA RECHERCHE DE PMSI A TRAITER TOUTES LES MINUTES
		ExecutorService execute = Executors.newSingleThreadExecutor();

		// TRAITEMENT TANT QU'UNE INTERRUPTION N'A PAS EU LIEU
		while (true) {
			// RECUPERATION DES PMSI A TRAITER :
			List<UploadedPmsi> elts = (new UploadedElementsDTO()).getUploadedElements(
					"SELECT plud_id, plud_processed, plud_finess, "
							+ "plud_year, plud_month, plud_dateenvoi, plud_rsf_oid oid, "
							+ "plud_rss_oid, plud_arguments FROM plud_pmsiupload where plud_processed = 'pending'::plud_status", new Object[] {});

			// TRAITEMENT DES PMSI UN PAR UN :
			for (UploadedPmsi elt : elts) {
				ProcessImpl processImpl = new ProcessImpl(elt);
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
			
			// RECUPERATION DES TABLES A NETTOYER
			ImportPmsiDTO ipd = new ImportPmsiDTO();
			List<Long> pludIds = ipd.getToCleanup();
			// TRAITEMENT DU NETTOYAGE TABLE PAR TABLE
			for (Long pludId : pludIds) {
				CleanupImpl cleanupImpl = new CleanupImpl(pludId);
				Future<Boolean> futureResult = execute.submit(cleanupImpl);
				// WAIT FOR THE RESULT OF COMPUTATION
				try {
					futureResult.get();
				} catch (InterruptedException e) {
					break;
				} catch (ExecutionException e) {
					log.warning("Erreur dans CleanupImpl : " + e.getMessage());
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
