package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FactsDetailsWindow extends Window {

	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public FactsDetailsWindow(Long pmel_root, Long pmel_position, String numfacture) {
		// TITLE
		super(numfacture);

		// SET VISUAL ASPECT
        setWidth("650px");
        setHeight("80%");
        setClosable(true);
        setResizable(true);
        setModal(true);
        setStyleName("details-factures");
        center();

        // SELECT LAYOUT
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

        // ADDS RSFB ELEMENTS
        Table rsfbTable = new Table();
        rsfbTable.setLocale(Locale.FRANCE);
        LazyQueryContainer lqcb = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfBDetailsQueryFactory(pmel_root, pmel_position));
       
        lqcb.addContainerProperty("pmel_id", Long.class, null, true, true);
        lqcb.addContainerProperty("pmel_line", Long.class, null, true, true);
        lqcb.addContainerProperty("formatteddatedebutsejour", String.class, null, true, true);
        lqcb.addContainerProperty("formatteddatefinsejour", String.class, null, true, true);
        lqcb.addContainerProperty("codeacte", String.class, null, true, true);
        lqcb.addContainerProperty("quantite", Integer.class, null, true, true);
        lqcb.addContainerProperty("numghs", String.class, null, true, true);
        lqcb.addContainerProperty("formattedmontanttotaldepense", String.class, null, true, true);

        rsfbTable.setContainerDataSource(lqcb);
        rsfbTable.setVisibleColumns(new Object[] {"pmel_line", "formatteddatedebutsejour", "formatteddatefinsejour", "codeacte", "quantite", "numghs", "formattedmontanttotaldepense"});
        rsfbTable.setColumnHeaders(new String[] {"Ligne", "Début séjour", "Fin séjour", "Acte", "Quantité", "GHS", "montant"} );
        rsfbTable.setSelectable(true);
        rsfbTable.setPageLength(4);
        rsfbTable.setWidth("100%");
        rsfbTable.setCaption("RSF B");
        
		try {
	        BaseRsfB summary = new NavigationActions().GetFacturesBSummary(pmel_root, pmel_position);
	        rsfbTable.setFooterVisible(true);
	        rsfbTable.setColumnFooter("formattedmontanttotaldepense", summary.getFormattedmontanttotaldepense());
		} catch (ActionException e) {
			Notification.show("Erreur de lecture du résumé des factures B", Notification.Type.WARNING_MESSAGE);
		}
        
        // ADDS RSFC ELEMENTS
        Table rsfcTable = new Table();
        rsfcTable.setLocale(Locale.FRANCE);
        LazyQueryContainer lqcc = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfCDetailsQueryFactory(pmel_root, pmel_position));
       
        lqcc.addContainerProperty("pmel_id", Long.class, null, true, true);
        lqcc.addContainerProperty("pmel_line", Long.class, null, true, true);
        lqcc.addContainerProperty("formatteddateacte", String.class, null, true, true);
        lqcc.addContainerProperty("codeacte", String.class, null, true, true);
        lqcc.addContainerProperty("quantite", Integer.class, null, true, true);
        lqcc.addContainerProperty("formattedmontanttotalhonoraire", String.class, null, true, true);

        rsfcTable.setContainerDataSource(lqcc);
        rsfcTable.setVisibleColumns(new Object[] {"pmel_line", "formatteddateacte", "codeacte", "quantite", "formattedmontanttotalhonoraire"});
        rsfcTable.setColumnHeaders(new String[] {"Ligne", "Date d'acte", "Code acte", "Quantité", "Montant"} );
        rsfcTable.setSelectable(true);
        rsfcTable.setPageLength(4);
        rsfcTable.setWidth("100%");
        rsfcTable.setCaption("RSF C");
        
		try {
	        BaseRsfC summary = new NavigationActions().GetFacturesCSummary(pmel_root, pmel_position);
	        rsfcTable.setFooterVisible(true);
	        rsfcTable.setColumnFooter("formattedmontanttotalhonoraire", summary.getFormattedmontanttotalhonoraire());
		} catch (ActionException e) {
			Notification.show("Erreur de lecture du résumé des factures C", Notification.Type.WARNING_MESSAGE);
		}

		layout.addComponent(rsfcTable);
        
	}

}
