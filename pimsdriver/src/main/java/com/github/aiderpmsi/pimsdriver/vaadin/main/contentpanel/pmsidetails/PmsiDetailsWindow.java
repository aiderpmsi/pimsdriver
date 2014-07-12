package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsidetails;

import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.vaadin.main.MenuBar;
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

	private final ArrayList<Table> tables;
	
	private final ServletContext context;
	
	public PmsiDetailsWindow(final Long pmel_root,
			final Long pmel_position,
			final MenuBar.MenuBarSelected type,
			final String typeLabel,
			final ServletContext context) {
		// TITLE
		super(type.getLabel() + " : " + typeLabel);

		this.context = context;
		
		// SET VISUAL ASPECT
        setWidth("650px");
        setHeight("80%");
        setClosable(true);
        setResizable(true);
        setModal(true);
        setStyleName("details-factures");
        center();

        // SELECT LAYOUT
        final VerticalLayout layout = new VerticalLayout();
        setContent(layout);

        switch (type) {
        case factures:
        	tables = new ArrayList<>(2);
        	tables.add(getRsfBTable(pmel_root, pmel_position));
        	tables.add(getRsfCTable(pmel_root, pmel_position));
        	break;
        case sejours:
        	tables = new ArrayList<>(3);
        	tables.add(getDA(pmel_root, pmel_position));
        	tables.add(getActe(pmel_root, pmel_position));
        	tables.add(getDAD(pmel_root, pmel_position));
        	break;
        default:
        	tables = new ArrayList<>();
        }
        for (final Table table : tables) {
        	layout.addComponent(table);
        }
	}

	public Table getActe(final Long pmel_root, final Long pmel_position) {
        // RSFB CONTAINER
		final LazyQueryContainer datasContainer = new LazyQueryContainer(
				new LazyQueryDefinition(false, 1000, "pmel_id"),
				new RssActeDetailsQueryFactory(pmel_root, pmel_position, context));

        // COLUMNS DEFINITIONS
		final LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("codeccam", String.class, "Acte", Align.CENTER),
        		new LazyColumnType("formatteddaterealisation", String.class, "Date", Align.CENTER),
        		new LazyColumnType("phase", String.class, "Phase", Align.CENTER),
        		new LazyColumnType("activite", String.class, "Activité", Align.CENTER),
        		new LazyColumnType("nbacte", String.class, "Nombre", Align.RIGHT)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setPageLength(4);
        table.setWidth("100%");
        table.setCaption("Actes");
        
        return table;
	}

	public Table getDA(final Long pmel_root, final Long pmel_position) {
        // RSFB CONTAINER
		final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RssDaDetailsQueryFactory(pmel_root, pmel_position, context));

        // COLUMNS DEFINITIONS
		final LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("da", String.class, "Diagnostic", Align.CENTER)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setPageLength(4);
        table.setWidth("100%");
        table.setCaption("Diagnostics associés significatifs");
        
        return table;
	}
	
	public Table getDAD(final Long pmel_root, final Long pmel_position) {
        // RSFB CONTAINER
		final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RssDadDetailsQueryFactory(pmel_root, pmel_position, context));

        // COLUMNS DEFINITIONS
		final LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("dad", String.class, "Diagnostic", Align.CENTER)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setPageLength(4);
        table.setWidth("100%");
        table.setCaption("Diagnostics associés documentaires");
        
        return table;
	}

	public Table getRsfBTable(final Long pmel_root, final Long pmel_position) {
        // RSFB CONTAINER
		final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfBDetailsQueryFactory(pmel_root, pmel_position, context));

        // COLUMNS DEFINITIONS
		final LazyColumnType[] cols = new LazyColumnType[] {
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
        final BaseRsfB summary = ActionEncloser.execute((exception) -> "Erreur de lecture du résumé des factures B",
        		() -> new NavigationActions(context).GetFacturesBSummary(pmel_root, pmel_position));
        table.setFooterVisible(true);
        table.setColumnFooter("formattedmontanttotaldepense", summary.getFormattedmontanttotaldepense());
        
        return table;
	}

	public Table getRsfCTable(final Long pmel_root, final Long pmel_position) {
        // RSFC CONTAINER
		final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new RsfCDetailsQueryFactory(pmel_root, pmel_position, context));

        // COLUMNS DEFINITIONS
		final LazyColumnType[] cols = new LazyColumnType[] {
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
        final BaseRsfC summary = ActionEncloser.execute((exception) -> "Erreur de lecture du résumé des factures C",
        		() -> new NavigationActions(context).GetFacturesCSummary(pmel_root, pmel_position));
        table.setFooterVisible(true);
        table.setColumnFooter("formattedmontanttotalhonoraire", summary.getFormattedmontanttotalhonoraire());
        
        return table;
	}
	
}
