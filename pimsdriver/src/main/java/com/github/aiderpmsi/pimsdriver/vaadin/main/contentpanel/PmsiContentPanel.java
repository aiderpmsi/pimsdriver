package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.NavSelectedEvent.Type;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyColumnType;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyTable;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser.ActionExecuter;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends VerticalLayout {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;

	/** header layout */
	private Layout header = null;
	
	/** body layout */
	private Layout body = null;
	
	public PmsiContentPanel() {
		super();
		addStyleName("pims-contentpanel");
		
		// CREATE A DEFAULT LAYOUT FOR BODY
		body = new VerticalLayout();

		// CREATE A DEFAULT LAYOUT FOR HEADER
		header = new VerticalLayout();
	
		// SETS LAYOUT HIERARCHY
		addComponent(header);
		addComponent(body);
	}
	
	public void setUpload(final UploadedPmsi model, UploadedPmsi.Status status) {
		// FIRST, CLEANUP BODY AND HEADER
		body.removeAllComponents();
		header.removeAllComponents();

		// IF STATUS IS NOT NULL AND SUCCESSED, ADD HEADER CONTENT
		if (model != null && status != null && status == UploadedPmsi.Status.successed)  {
			ActionEncloser.execute(new ActionExecuter() {
				@Override
				public void action() throws ActionException {
					// GETS THE SUMMARY OF RSF AND RSS
					NavigationActions.Overview overview = new NavigationActions().getOverview(model);
					
					// CREATES THE CONFIG TABLE
					Object[][] headers = new Object[][] {
							{"RSF", overview.rsf},
							{overview.rss == null ? "Absence de RSS" : "RSS",
									overview.rss == null ? new ArrayList<NavigationDTO.PmsiOverviewEntry>() : overview.rss}
					};
					
					// USE THIS CONFIG TABLE TO FILL THE HEADER
					fillContentHeader(headers);
				}
				@Override
				public String msgError(ActionException e) {
					return "Erreur lors de la récupération des éléments des rsf et rss ";
				}
			});
		}
	}
	
	public void show(Type type, UploadedPmsi model) {
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
	
	private Table createSejoursTable(final Type type, final UploadedPmsi model) {
        // RSS MAIN CONTAINER
        LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new SejoursQueryFactory(model.recordid));
		
        // COLUMNS DEFINITIONS
        LazyColumnType[] cols = new LazyColumnType[] {
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
        table.addActionHandler(new PmsiSelectedHandler(type, datasContainer, model.recordid));

        return table;
	}

	private Table createFactTable(final Type type, final UploadedPmsi model) {
        // RSFA CONTAINER
        LazyQueryContainer datasContainer = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new FacturesQueryFactory(model.recordid));

        // COLUMNS DEFINITIONS
        LazyColumnType[] cols = new LazyColumnType[] {
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
        table.addActionHandler(new PmsiSelectedHandler(type, datasContainer, model.recordid));

        // EXECUTE AN ACTION
        ActionEncloser.execute(new ActionEncloser.ActionExecuter() {
			@Override
			public void action() throws ActionException {
		        BaseRsfA summary = new NavigationActions().GetFacturesSummary(model.recordid);
		        table.setFooterVisible(true);
		        table.setColumnFooter("formattedtotalfacturehonoraire", summary.getFormattedtotalfacturehonoraire());
		        table.setColumnFooter("formattedtotalfactureph", summary.getFormattedtotalfactureph());
			}
			@Override
			public String msgError(ActionException e) {
				return "Erreur de lecture du résumé des factures";
			}
		});
        
        return table;
	}
	
	private void fillContentHeader(Object[][] configs) {

		for (Object[] config : configs) {
			@SuppressWarnings("unchecked")
			Layout layout = createContentHeader((String) config[0], (List<NavigationDTO.PmsiOverviewEntry>) config[1]);
			header.addComponent(layout);
		}

	}
	
	private Layout createContentHeader(String header, List<NavigationDTO.PmsiOverviewEntry> entries) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName("pims-contentpanel-header-layout");

		// TITLE
		Label title = new Label(header);
		title.addStyleName("pims-contentpanel-header-label");
		layout.addComponent(title);
		
		// CONTENT
		CssLayout contentLayout = new CssLayout();
		contentLayout.addStyleName("pims-contentpanel-header-content-layout");
		for (NavigationDTO.PmsiOverviewEntry entry : entries) {
			Label label = new Label(entry.lineName + " : " + Long.toString(entry.number));
			label.setSizeUndefined();
			label.addStyleName("pims-contentpanel-header-content-label");
			contentLayout.addComponent(label);
		}
		layout.addComponent(contentLayout);
		return layout;
	}

	
}
