package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

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
        LazyQueryContainer lqc = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "recordid"),
        		new RsfBDetailsQueryFactory(pmel_root, pmel_position));
        lqc.addContainerProperty("pmel_line", Long.class, null, true, true);
        lqc.addContainerProperty("datedebutsejour", String.class, null, true, true);
        lqc.addContainerProperty("datefinsejour", String.class, null, true, true);
        lqc.addContainerProperty("codeacte", String.class, null, true, true);
        lqc.addContainerProperty("quantite", String.class, null, true, true);
        lqc.addContainerProperty("numghs", String.class, null, true, true);
        lqc.addContainerProperty("montanttotaldepense", String.class, null, true, true);

        rsfbTable.setContainerDataSource(lqc);
        rsfbTable.setVisibleColumns(new Object[] {"pmel_line", "datedebutsejour", "datefinsejour", "codeacte", "quantite", "numghs", "montanttotaldepense"});
        rsfbTable.setColumnHeaders(new String[] {"Ligne", "Début séjour", "Fin séjour", "Acte", "Quantité", "GHS", "montant"} );
        rsfbTable.setSelectable(true);
        rsfbTable.setSizeFull();
        
        layout.addComponent(rsfbTable);
        
	}

}
