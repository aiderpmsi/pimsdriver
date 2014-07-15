package com.github.aiderpmsi.pimsdriver.processor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.CleanupActions;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.db.actions.ProcessActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class ProcessTask implements Callable<Boolean> {

	private static final Logger log = Logger.getLogger(ProcessTask.class.toString());

	private final ServletContext context;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(1);

	private final List<Filter> processFilters = new ArrayList<>(1);

	private final LinkedList<Future<Boolean>> futures = new LinkedList<>();
	
	public ProcessTask(final ServletContext context) {
		this.context = context;
		processFilters.add(new Compare.Equal("plud_processed", UploadedPmsi.Status.pending));
	}

	/** LANCEMENT DE LA RECHERCHE DE PMSI A TRAITER TOUTES LES MINUTES */
	@Override
	public Boolean call() throws Exception {

		// GESTIONNAIRES D'ACTION
		final NavigationActions na = new NavigationActions(context);
		final CleanupActions cu = new CleanupActions(context);
		final ProcessActions ac = new ProcessActions(context);
		
		// TRAITEMENT TANT QU'UNE INTERRUPTION N'A PAS EU LIEU
		while (true) {
			
			// GESTION DES PMSI A TRAITER :
			
			try {
				// RECUPERATION DES PMSI A TRAITER :
				final List<UploadedPmsi> elts = na.getUploadedPmsi(processFilters, new ArrayList<OrderBy>(0), 0, 10);
				
				// TRAITEMENT DES PMSI UN PAR UN :
				for (UploadedPmsi elt : elts) {
					futures.addLast(executorService.submit(
							() -> ac.processPmsi(elt)));
				}
				
				// WAIT FOR EACH PROCESS TO BE FINISHED
				waitFuturesFinish();
			} catch (ActionException e) {
				// DO NOTHING
			}

			// GESTION DES PMSI A SUPPRIMER :
			try {
				// RECUPERATION DES TABLES A NETTOYER
				final List<Long> pludIds = cu.getToCleanup();
				
				// TRAITEMENT DU NETTOYAGE TABLE PAR TABLE
				for (final Long pludId : pludIds) {
				futures.addLast(executorService.submit(
						() -> cu.cleanup(pludId)));
				}

				// WAIT FOR THE RESULT OF COMPUTATION
				waitFuturesFinish();
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

	private void waitFuturesFinish() {
		while (futures.size() != 0) {
			try {
				for (;;) {
					try {
						// TRY TO GET FUTURE, WAIT 1000 MS BETWEEN SEEING IF THREAD HAS BEEN INTERRUPTED
						futures.getFirst().get(1000, TimeUnit.MILLISECONDS);
						// IF THIS ITEM IS TREATED, REMOVE FROM THE LIST
						futures.removeFirst();
						break;
					} catch (TimeoutException e) {
						// DO NOTHING, RETRY
					}
				}
			} catch (InterruptedException e) {
				// THREAD HAS BEEN INTERRUPTED, STOP
				futures.removeFirst();
				break;
			} catch (ExecutionException e) {
				futures.removeFirst();
				log.warning("Erreur dans ProcessImpl : " + e.getMessage());
			}
		}
	}	
}
