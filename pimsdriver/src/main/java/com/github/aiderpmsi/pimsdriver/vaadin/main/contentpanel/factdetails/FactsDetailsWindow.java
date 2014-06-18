package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.factdetails;

import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyColumnType;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyTable;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FactsDetailsWindow extends Window {

	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public FactsDetailsWindow(final Long pmel_root, final Long pmel_position, String numfacture) {
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

        // RSFB CONTAINER
        LazyQueryContainer lqcb = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfBDetailsQueryFactory(pmel_root, pmel_position));

        // COLUMNS DEFINITIONS
        LazyColumnType[] columnsb = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("formatteddatedebutsejour", String.class, "Début séjour", Align.CENTER),
        		new LazyColumnType("formatteddatefinsejour", String.class, "Fin séjour", Align.CENTER),
        		new LazyColumnType("codeacte", String.class, "Acte", Align.CENTER),
        		new LazyColumnType("quantite", String.class, "Quantité", Align.RIGHT),
        		new LazyColumnType("numghs", String.class, "GHS", Align.CENTER),
        		new LazyColumnType("formattedmontanttotaldepense", String.class, "Entrée", Align.RIGHT)
        };

        final Table rsfbTable = new LazyTable(columnsb, Locale.FRANCE, lqcb);

        rsfbTable.setSelectable(true);
        rsfbTable.setPageLength(4);
        rsfbTable.setWidth("100%");
        rsfbTable.setCaption("RSF B");

        // EXECUTE AN ACTION
        ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
		        BaseRsfB summary = new NavigationActions().GetFacturesBSummary(pmel_root, pmel_position);
		        rsfbTable.setFooterVisible(true);
		        rsfbTable.setColumnFooter("formattedmontanttotaldepense", summary.getFormattedmontanttotaldepense());
			}
			@Override
			public String msgError(ActionException e) {
				return "Erreur de lecture du résumé des factures B";
			}
		});
        
		layout.addComponent(rsfbTable);

        // RSFB CONTAINER
        LazyQueryContainer lqcc = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfCDetailsQueryFactory(pmel_root, pmel_position));

        // COLUMNS DEFINITIONS
        LazyColumnType[] columnsc = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("formatteddateacte", String.class, "Date", Align.CENTER),
        		new LazyColumnType("codeacte", String.class, "Acte", Align.CENTER),
        		new LazyColumnType("quantite", String.class, "Quantité", Align.RIGHT),
        		new LazyColumnType("formattedmontanttotalhonoraire", String.class, "Entrée", Align.RIGHT)
        };

        final Table rsfcTable = new LazyTable(columnsc, Locale.FRANCE, lqcc);
        
        rsfcTable.setSelectable(true);
        rsfcTable.setPageLength(4);
        rsfcTable.setWidth("100%");
        rsfcTable.setCaption("RSF C");
        
        // EXECUTE AN ACTION
        ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
		        BaseRsfC summary = new NavigationActions().GetFacturesCSummary(pmel_root, pmel_position);
		        rsfcTable.setFooterVisible(true);
		        rsfcTable.setColumnFooter("formattedmontanttotalhonoraire", summary.getFormattedmontanttotalhonoraire());
			}
			@Override
			public String msgError(ActionException e) {
				return "Erreur de lecture du résumé des factures C";
			}
		});

		layout.addComponent(rsfcTable);
        
	}

}
