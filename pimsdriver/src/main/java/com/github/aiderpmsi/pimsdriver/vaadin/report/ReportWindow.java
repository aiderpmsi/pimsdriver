package com.github.aiderpmsi.pimsdriver.vaadin.report;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;

public class ReportWindow extends Window {

	/** Serial Id */
	private static final long serialVersionUID = 5355233025554029932L;

	/** Associated pmsi */
	private UploadedPmsi pmsi;
	
	public ReportWindow(UploadedPmsi pmsi) {
		// TITLE
		super("Reporting");

		this.pmsi = pmsi;

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
		Link report = new Link("Rapport", new ExternalResource("rest/report/report/" + pmsi.recordid + "/factures.pdf"));
		report.setTargetName("facture" + pmsi.finess + ".pdf");
		hl.addComponent(report);
	}

}