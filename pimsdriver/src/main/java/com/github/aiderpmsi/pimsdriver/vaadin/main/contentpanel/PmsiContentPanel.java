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
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyColumnType;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.LazyTable;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser;
import com.github.aiderpmsi.pimsdriver.vaadin.utils.aop.ActionEncloser.ActionExecuter;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;

	/** content layout */
	private Layout content = null;
	
	/** header layout */
	private Layout header = null;
	
	/** body layout */
	private Layout body = null;
	
	public PmsiContentPanel() {
		super();
		setCaption(null);
		addStyleName("pims-contentpanel");
		
		// CREATES A DEFAULT LAYOUT FOR CONTENT
		content = new VerticalLayout();
		
		// CREATE A DEFAULT LAYOUT FOR BODY
		body = new VerticalLayout();

		// CREATE A DEFAULT LAYOUT FOR HEADER
		header = new VerticalLayout();
	
		// SETS LAYOUT HIERARCHY
		content.addComponent(header);
		content.addComponent(body);
		setContent(content);
	}
	
	public void setUpload(final UploadedPmsi model, UploadedPmsi.Status status) {
		// FIRST, CLEANUP BODY AND HEADER
		body.removeAllComponents();
		header.removeAllComponents();
		// CLEANUP HANDLERS
		removeAllActionHandlers();
		
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
	
	public void showFactures(UploadedPmsi model) {
		// REMOVE ALL COMPONENTS OF BODY
		body.removeAllComponents();

		// CREATES THE CONTAINER
        LazyQueryContainer lqc = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new FacturesQueryFactory(model.recordid));

        // CREATES THE TABLE
        LazyColumnType[] columns = new LazyColumnType[] {
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

        Table processtable = new LazyTable(columns, Locale.FRANCE, lqc);
        processtable.setSelectable(true);
        processtable.setSizeFull();

		try {
	        BaseRsfA summary = new NavigationActions().GetFacturesSummary(model.recordid);
	        processtable.setFooterVisible(true);
	        processtable.setColumnFooter("formattedtotalfacturehonoraire", summary.getFormattedtotalfacturehonoraire());
	        processtable.setColumnFooter("formattedtotalfactureph", summary.getFormattedtotalfactureph());
		} catch (ActionException e) {
			Notification.show("Erreur de lecture du résumé des factures", Notification.Type.WARNING_MESSAGE);
		}
        
        processtable.setSizeFull();
        
        processtable.addActionHandler(new FactureSelectedHandler(lqc));
        
        body.addComponent(processtable);
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
