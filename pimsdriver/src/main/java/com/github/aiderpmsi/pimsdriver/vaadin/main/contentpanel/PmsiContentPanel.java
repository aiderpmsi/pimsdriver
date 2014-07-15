package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.MenuBar;
import com.github.aiderpmsi.pimsdriver.vaadin.main.SplitPanel;
import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsidetails.PmsiDetailsWindow;
import com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel.pmsisource.PmsiSourceWindow;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyColumnType;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyTable;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionHandlerEncloser;
import com.vaadin.event.Action;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends VerticalLayout {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;

	/** header layout */
	private final VerticalLayout header = new VerticalLayout();
	
	/** body layout */
	private final VerticalLayout body = new VerticalLayout();
	
	private final SplitPanel splitPanel;
	
	public PmsiContentPanel(SplitPanel splitPanel) {
		super();
		addStyleName("pims-contentpanel");
	
		// SETS LAYOUT HIERARCHY
		addComponent(header);
		addComponent(body);
		
		// SETS PARENT
		this.splitPanel = splitPanel;
	}
	
	public void setUpload(final UploadedPmsi model) {
		// FIRST, CLEANUP BODY AND HEADER
		body.removeAllComponents();
		header.removeAllComponents();

		// IF STATUS IS NOT NULL AND SUCCESSED, ADD HEADER CONTENT
		if (model != null && model.getStatus() != null && model.getStatus() == UploadedPmsi.Status.successed)  {
			final NavigationActions.Overview overview = ActionEncloser.execute(
					(exception) -> "Erreur lors de la récupération des éléments des rsf et rss ", 
					() -> new NavigationActions(getSplitPanel().getRootWindow().getMainApplication().getServletContext()).getOverview(model));
			
			// CREATES THE CONFIG TABLE
			final Object[][] headers = new Object[][] {
					{"RSF", overview.rsf},
					{overview.rss == null ? "Absence de RSS" : "RSS",
							overview.rss == null ? new ArrayList<NavigationDTO.PmsiOverviewEntry>() : overview.rss}
			};
			
			fillContentHeader(headers);
		}
	}
	
	public void show(MenuBar.MenuBarSelected type, UploadedPmsi model) {
		// REMOVE ALL COMPONENTS OF BODY
		body.removeAllComponents();

		// CREATES THE CORRESPONDING TABLE
		switch (type) {
		case sejours:
			body.addComponent(createSejoursTable(type, model));
			break;
		case factures:
			body.addComponent(createFactTable(type, model));
			break;
		}
	}
	
	private Table createSejoursTable(final MenuBar.MenuBarSelected type, final UploadedPmsi model) {
        // RSS MAIN CONTAINER
        final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new SejoursQueryFactory(model.recordid, getSplitPanel().getRootWindow().getMainApplication().getServletContext()));
		
        // COLUMNS DEFINITIONS
        final LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("numrss", String.class, "Rss", Align.LEFT),
        		new LazyColumnType("numlocalsejour", String.class, "Séjour", Align.LEFT),
        		new LazyColumnType("numrum", String.class, "Rum", Align.LEFT),
        		new LazyColumnType("numunitemedicale", String.class, "Unité", Align.LEFT),
        		new LazyColumnType("ghm", String.class, "GHM", Align.CENTER),
        		new LazyColumnType("ghmcorrige", String.class, "GHM corrigé", Align.CENTER),
        		new LazyColumnType("dp", String.class, "DP", Align.CENTER),
        		new LazyColumnType("dr", String.class, "DR", Align.CENTER),
        		new LazyColumnType("nbseances", String.class, "Séances", Align.RIGHT),
        		new LazyColumnType("formatteddateentree", String.class, "Entrée", Align.CENTER),
        		new LazyColumnType("formatteddatesortie", String.class, "Sortie", Align.CENTER)
        };

        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setSizeFull();
        table.setCaption("Séjours");

        return table;
	}

	private Table createFactTable(final MenuBar.MenuBarSelected type, final UploadedPmsi model) {
        // RSFA CONTAINER
        final LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new FacturesQueryFactory(model.recordid, getSplitPanel().getRootWindow().getMainApplication().getServletContext()));

        // COLUMNS DEFINITIONS
        final LazyColumnType[] cols = new LazyColumnType[] {
        		new LazyColumnType("pmel_id", Long.class, null, null),
        		new LazyColumnType("pmel_line", Long.class, "Ligne", Align.RIGHT),
        		new LazyColumnType("numfacture", String.class, "Facture", Align.LEFT),
        		new LazyColumnType("numrss", String.class, "Rss", Align.LEFT),
        		new LazyColumnType("codess", String.class, "Code Sécu", Align.LEFT),
        		new LazyColumnType("sexe", String.class, "Sexe", Align.CENTER),
        		new LazyColumnType("formatteddatenaissance", String.class, "Naissance", Align.CENTER),
        		new LazyColumnType("formatteddateentree", String.class, "Entrée", Align.CENTER),
        		new LazyColumnType("formatteddatesortie", String.class, "Sortie", Align.CENTER),
        		new LazyColumnType("formattedtotalfacturehonoraire", String.class, "Honoraires", Align.RIGHT),
        		new LazyColumnType("formattedtotalfactureph", String.class, "Prestations", Align.RIGHT),
        		new LazyColumnType("etatliquidation", String.class, "Liquidation", Align.RIGHT)
        };
        
        final Table table = new LazyTable(cols, Locale.FRANCE, datasContainer);

        table.setSelectable(true);
        table.setSizeFull();
        table.setCaption("Factures");

        // FILLS THE SUMMARY
        final BaseRsfA summary = ActionEncloser.execute((exception) -> "Erreur de lecture du résumé des factures",
        		() -> new NavigationActions(getSplitPanel().getRootWindow().getMainApplication().getServletContext()).GetFacturesSummary(model.recordid));
        
        table.setFooterVisible(true);
        table.setColumnFooter("formattedtotalfacturehonoraire", summary.getFormattedtotalfacturehonoraire());
        table.setColumnFooter("formattedtotalfactureph", summary.getFormattedtotalfactureph());
        
        table.addActionHandler(
        		new ActionHandlerEncloser(ACTIONS[0], (action, sender, target) -> {
        			final Long pmel_position = (Long) datasContainer.getContainerProperty(target, "pmel_position").getValue();
    				String numfacture = (String) datasContainer.getContainerProperty(target, "numfacture").getValue();
        			UI.getCurrent().addWindow(new PmsiDetailsWindow(model.recordid, pmel_position, type, numfacture, getSplitPanel().getRootWindow().getMainApplication().getServletContext()));
        		}));
        table.addActionHandler(
        		new ActionHandlerEncloser(ACTIONS[1], (action, sender, target) -> {
        			final Long pmel_position = (Long) datasContainer.getContainerProperty(target, "pmel_position").getValue();
    				String numfacture = (String) datasContainer.getContainerProperty(target, "numfacture").getValue();
        			UI.getCurrent().addWindow(new PmsiSourceWindow(model.recordid, pmel_position, type, numfacture, getSplitPanel().getRootWindow().getMainApplication().getServletContext()));
        		}));
        return table;
	}
	
	private void fillContentHeader(Object[][] configs) {

		for (final Object[] config : configs) {
			@SuppressWarnings("unchecked")
			final Layout layout = createContentHeader((String) config[0], (List<NavigationDTO.PmsiOverviewEntry>) config[1]);
			header.addComponent(layout);
		}

	}
	
	private Layout createContentHeader(String header, List<NavigationDTO.PmsiOverviewEntry> entries) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName("pims-contentpanel-header-layout");

		// TITLE
		final Label title = new Label(header);
		title.addStyleName("pims-contentpanel-header-label");
		layout.addComponent(title);
		
		// CONTENT
		final CssLayout contentLayout = new CssLayout();
		contentLayout.addStyleName("pims-contentpanel-header-content-layout");
		for (final NavigationDTO.PmsiOverviewEntry entry : entries) {
			Label label = new Label(entry.lineName + " : " + Long.toString(entry.number));
			label.setSizeUndefined();
			label.addStyleName("pims-contentpanel-header-content-label");
			contentLayout.addComponent(label);
		}
		layout.addComponent(contentLayout);
		return layout;
	}

	public SplitPanel getSplitPanel() {
		return splitPanel;
	}

	private static final Action[] ACTIONS = new Action[] {new Action("détails"), new Action("source")};

}
