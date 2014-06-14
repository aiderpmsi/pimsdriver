package com.github.aiderpmsi.pimsdriver.vaadin.report;

import java.util.LinkedList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;

public class ReportWindow extends Window {

	/** Serial Id */
	private static final long serialVersionUID = 5355233025554029932L;

	/** enum of possible reports categories */
	public enum Category {
		factures, sejours;
		
		public List<Link> getReports(UploadedPmsi pmsi) {
			LinkedList<Link> links = new LinkedList<>();
			
			switch (this) {
			case factures:
				links.add(new Link("Factures triées selon numéro de facture", new ExternalResource("rest/report/report/" + pmsi.recordid + "/factures.pdf?order=facture")));
				links.getLast().setTargetName("facture_" + pmsi.finess + ".pdf");
				links.add(new Link("Factures ordonnées selon ordre du rsf", new ExternalResource("rest/report/report/" + pmsi.recordid + "/factures.pdf?order=rsf")));
				links.getLast().setTargetName("facture_" + pmsi.finess + ".pdf");
				break;
			case sejours:
				links.add(new Link("Séjours triés selon numéro de rss", new ExternalResource("rest/report/report/" + pmsi.recordid + "/sejours.pdf")));
				links.getLast().setTargetName("sejour_" + pmsi.finess + ".pdf");
				break;
			}
			return links;
		}
	}
	
	public ReportWindow(UploadedPmsi pmsi, Category category) {
		// TITLE
		super("Reporting");

		// SET VISUAL ASPECT
		setWidth("650px");
		setClosable(true);
		setResizable(true);
		setModal(true);
		setStyleName("reportpmsi");
		center();

        // SELECT LAYOUT
        HorizontalLayout hl = new HorizontalLayout();
        setContent(hl);

		// CREATE LINK TO JAX-RS TO EXPORT ELEMENT
        for (Link link : category.getReports(pmsi)) {
        	hl.addComponent(link);
        }
	}

}