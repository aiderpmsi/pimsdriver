package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.PmsiOverviewEntry;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;
	
	public PmsiContentPanel() {
		super();
		setCaption(null);
		setVisible(false);
		addStyleName("pims-contentpanel");
	}
	
	public void setUpload(UploadedPmsi model, UploadedPmsi.Status status) {
		// IF MODEL AND STATUS ARE NULL, REMOVE EVERYTHING IN PANEL
		if (model == null)  {
			this.removeAllActionHandlers();
			this.setContent(new VerticalLayout());
			setVisible(false);
		}
		// IF STATUS IS FAILED, WE HAVE TO REMOVE EVERYTHING OF THIS PANEL
		else if (status == UploadedPmsi.Status.failed) {
			this.removeAllActionHandlers();
			this.setContent(new VerticalLayout());
			setVisible(false);
		}
		else {
			try {
				NavigationActions na = new NavigationActions();
				// GETS THE OVERVIEW OF RSF AND RSS
				NavigationActions.Overview overview = na.getOverview(model);

				// SETS THE LAYOUT
				VerticalLayout principallayout = new VerticalLayout();
				setContent(principallayout);
				
				VerticalLayout headerlayout = new VerticalLayout();
				principallayout.addStyleName("pims-contentpanel-headerlayout");
				principallayout.addComponent(headerlayout);
				
				// RSF PANEL
				Layout rsfPanel = createContentHeader("RSF", overview.rsf);
				headerlayout.addComponent(rsfPanel);
				
				// RSS PANEL
				Layout rssPanel;
				if (overview.rss == null) {
					// THERE IS NO RSS FILE
					rssPanel = createContentHeader("Absence de RSS", new ArrayList<PmsiOverviewEntry>());
				} else {
					// FILLS THE RSS CONTENT
					rssPanel = createContentHeader("RSS", overview.rss);
				}
				headerlayout.addComponent(rssPanel);
				
				setVisible(true);
			} catch (ActionException e) {
				Notification.show("Erreur lors de la récupération des éléments des rsf et rss ", Notification.Type.WARNING_MESSAGE);
			}
		}
	}
	
	private Layout createContentHeader(String header, List<PmsiOverviewEntry> entries) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.addStyleName("pims-contentpanel-header-layout");

		// TITLE
		Label title = new Label(header);
		title.addStyleName("pims-contentpanel-header-label");
		layout.addComponent(title);
		
		// CONTENT
		CssLayout contentLayout = new CssLayout();
		contentLayout.addStyleName("pims-contentpanel-header-content-layout");
		for (PmsiOverviewEntry entry : entries) {
			Label label = new Label(entry.lineName + " : " + Long.toString(entry.number));
			label.setSizeUndefined();
			label.addStyleName("pims-contentpanel-header-content-label");
			contentLayout.addComponent(label);
		}
		layout.addComponent(contentLayout);
		return layout;
	}
	
}
