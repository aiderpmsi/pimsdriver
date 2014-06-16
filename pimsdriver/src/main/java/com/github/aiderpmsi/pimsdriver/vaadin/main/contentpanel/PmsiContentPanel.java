package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;
	
	/** body layout */
	private Layout body = null;
	
	public PmsiContentPanel() {
		super();
		setCaption(null);
		setVisible(false);
		addStyleName("pims-contentpanel");
		
		// SETS THE LAYOUT
		setContent(new VerticalLayout());
	}
	
	public void setUpload(UploadedPmsi model, UploadedPmsi.Status status) {
		// IF MODEL AND STATUS ARE NULL, OR STATUS IS FAILED, REMOVE EVERYTHING IN PANEL
		if (model == null)  {
			removeAllActionHandlers();
			setContent(new VerticalLayout());
			setVisible(false);
			
			body = null;
		}
		else {
			try {
				NavigationActions na = new NavigationActions();
				// GETS THE OVERVIEW OF RSF AND RSS
				NavigationActions.Overview overview = na.getOverview(model);

				// SETS THE GENERAL LAYOUT
				setContent(new VerticalLayout());
				getContent().addStyleName("pims-contentpanel-headerlayout");
				
				// SETS THE HEADERLAYOUT
				VerticalLayout headerlayout = new VerticalLayout();
				((Layout) getContent()).addComponent(headerlayout);

				// RSF PANEL
				Layout rsfPanel = createContentHeader("RSF", overview.rsf);
				headerlayout.addComponent(rsfPanel);
				
				// RSS PANEL
				Layout rssPanel = (overview.rss == null) ?
						createContentHeader("Absence de RSS", new ArrayList<NavigationDTO.PmsiOverviewEntry>()) :
							createContentHeader("RSS", overview.rss);;
				headerlayout.addComponent(rssPanel);

				// SETS THE LAYOUT FOR THE BODY
				body = new VerticalLayout();
				((Layout) getContent()).addComponent(body);
				
				setVisible(true);
			} catch (ActionException e) {
				Notification.show("Erreur lors de la récupération des éléments des rsf et rss ", Notification.Type.WARNING_MESSAGE);
			}
		}
	}
	
	public void showFactures(UploadedPmsi model) {
		// REMOVE ALL COMPONENTS OF BODY
		body.removeAllComponents();

		// CREATE THE TABLE
        Table processtable = new Table();
        processtable.setLocale(Locale.FRANCE);
        LazyQueryContainer lqc = new LazyQueryContainer(
        		new LazyQueryDefinition(false, 1000, "pmel_id"),
        		new FacturesQueryFactory(model.recordid));

        lqc.addContainerProperty("pmel_id", Long.class, null, true, true);
        lqc.addContainerProperty("ligne", String.class, null, true, true);
        lqc.addContainerProperty("numfacture", String.class, null, true, true);
        lqc.addContainerProperty("numrss", String.class, null, true, true);
        lqc.addContainerProperty("codess", String.class, null, true, true);
        lqc.addContainerProperty("sexe", String.class, null, true, true);
        lqc.addContainerProperty("datenaissance", String.class, null, true, true);
        lqc.addContainerProperty("dateentree", String.class, null, true, true);
        lqc.addContainerProperty("datesortie", String.class, null, true, true);
        lqc.addContainerProperty("totalfacturehonoraire", String.class, null, true, true);
        lqc.addContainerProperty("totalfactureph", String.class, null, true, true);
        lqc.addContainerProperty("etatliquidation", String.class, null, true, true);
        
        processtable.setContainerDataSource(lqc);
        processtable.setVisibleColumns(new Object[] {"ligne", "numfacture", "numrss", "codess", "sexe", "datenaissance", "dateentree", "datesortie", "totalfacturehonoraire", "totalfactureph", "etatliquidation"});
        processtable.setColumnHeaders(new String[] {"Ligne", "Facture", "Rss", "Code sécu", "Sexe", "Naissance", "Entrée", "Sortie", "Honoraires", "Prestations", "Liquidation"} );
        processtable.setSelectable(true);
        processtable.setSizeFull();
        
        processtable.addActionHandler(new FactureSelectedHandler(lqc));
        
        body.addComponent(processtable);
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
