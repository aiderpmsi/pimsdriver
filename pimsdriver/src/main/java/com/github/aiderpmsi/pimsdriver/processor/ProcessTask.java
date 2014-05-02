package com.github.aiderpmsi.pimsdriver.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.CleanupActions;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class ProcessTask implements Callable<Boolean> {

	static final Logger log = Logger.getLogger(ProcessTask.class.toString());

	@Override
	public Boolean call() throws Exception {
		// LANCEMENT DE LA RECHERCHE DE PMSI A TRAITER TOUTES LES MINUTES
		ExecutorService execute = Executors.newSingleThreadExecutor();

		// CREATION DES FILTRES POUR LA RECUPERATION DES PMSI
		List<Filter> filter = new ArrayList<>();
		filter.add(new Compare.Equal("plud_processed", UploadedPmsi.Status.pending));
		
		// GESTIONNAIRE D'ACTION
		NavigationActions na = new NavigationActions();
		CleanupActions cu = new CleanupActions();
		
		// TRAITEMENT TANT QU'UNE INTERRUPTION N'A PAS EU LIEU
		while (true) {
			
			// GESTION DES PMSI A TRAITER :
			
			try {
				// RECUPERATION DES PMSI A TRAITER :
				List<UploadedPmsi> elts = na.getUploadedPmsi(filter, new ArrayList<OrderBy>(0), 0, 10);
				
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
			} catch (ActionException e) {
				// DO NOTHING
			}

			// GESTION DES PMSI A SUPPRIMER :

			try {
				// RECUPERATION DES TABLES A NETTOYER
				List<Long> pludIds = cu.getToCleanup();
				
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
			} catch (ActionException e) {
				// DO NOTHING
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
