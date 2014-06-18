package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsidetails;

import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.vaadin.main.NavSelectedEvent.Type;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyColumnType;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyTable;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PmsiDetailsWindow extends Window {
	
	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public PmsiDetailsWindow(final Long pmel_root, final Long pmel_position, final Type type, final String typeLabel) {
		// TITLE
		super(type.getLabel() + " : " + typeLabel);

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

        switch (type) {
        case factures:
        	// RSFB TABLE
        	Table rsfB = getRsfBTable(pmel_root, pmel_position);
        	layout.addComponent(rsfB);

        	// RSFC TABLE
        	Table rsfC = getRsfCTable(pmel_root, pmel_position);
        	layout.addComponent(rsfC);
        	break;
        case sejours:
        	break;
        }
	}

	public Table getRsfBTable(final Long pmel_root, final Long pmel_position) {
        // RSFB CONTAINER
        LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfBDetailsQueryFactory(pmel_root, pmel_position));

        // COLUMNS DEFINITIONS
        LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("formatteddatedebutsejour", String.class, "Début séjour", Align.CENTER),
        		new LazyColumnType("formatteddatefinsejour", String.class, "Fin séjour", Align.CENTER),
        		new LazyColumnType("codeacte", String.class, "Acte", Align.CENTER),
        		new LazyColumnType("quantite", String.class, "Quantité", Align.RIGHT),
        		new LazyColumnType("numghs", String.class, "GHS", Align.CENTER),
        		new LazyColumnType("formattedmontanttotaldepense", String.class, "Entrée", Align.RIGHT)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setPageLength(4);
        table.setWidth("100%");
        table.setCaption("RSF B");

        // EXECUTE AN ACTION
        ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
		        BaseRsfB summary = new NavigationActions().GetFacturesBSummary(pmel_root, pmel_position);
		        table.setFooterVisible(true);
		        table.setColumnFooter("formattedmontanttotaldepense", summary.getFormattedmontanttotaldepense());
			}
			@Override
			public String msgError(ActionException e) {
				return "Erreur de lecture du résumé des factures B";
			}
		});
        
        return table;
	}

	public Table getRsfCTable(final Long pmel_root, final Long pmel_position) {
        // RSFC CONTAINER
        LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfCDetailsQueryFactory(pmel_root, pmel_position));

        // COLUMNS DEFINITIONS
        LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("formatteddateacte", String.class, "Date", Align.CENTER),
        		new LazyColumnType("codeacte", String.class, "Acte", Align.CENTER),
        		new LazyColumnType("quantite", String.class, "Quantité", Align.RIGHT),
        		new LazyColumnType("formattedmontanttotalhonoraire", String.class, "Entrée", Align.RIGHT)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);
        
        table.setSelectable(true);
        table.setPageLength(4);
        table.setWidth("100%");
        table.setCaption("RSF C");
        
        // EXECUTE AN ACTION
        ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
		        BaseRsfC summary = new NavigationActions().GetFacturesCSummary(pmel_root, pmel_position);
		        table.setFooterVisible(true);
		        table.setColumnFooter("formattedmontanttotalhonoraire", summary.getFormattedmontanttotalhonoraire());
			}
			@Override
			public String msgError(ActionException e) {
				return "Erreur de lecture du résumé des factures C";
			}
		});
        
        return table;
	}
	
}
