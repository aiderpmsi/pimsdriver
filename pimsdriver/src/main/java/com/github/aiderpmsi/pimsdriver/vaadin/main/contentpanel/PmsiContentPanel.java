package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
	}
	
	public void setUpload(UploadedPmsi model, UploadedPmsi.Status status) {
		// IF STATUS IS FAILED, WE HAVE TO REMOVE EVERYTHIN OF THIS PANEL
		if (status == UploadedPmsi.Status.failed) {
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
				
				HorizontalLayout headerlayout = new HorizontalLayout();
				principallayout.addStyleName("pims-contentpanel-headerlayout");
				principallayout.addComponent(headerlayout);
				
				// RSF PANEL
				Panel rsfPanel = createPanel("RSF", new String[][] {
						new String[] {"Nb lignes A", overview.rsf.getRsfa().toString()},
						new String[] {"Nb lignes B", overview.rsf.getRsfb().toString()},
						new String[] {"Nb lignes C", overview.rsf.getRsfc().toString()},
						new String[] {"Nb lignes H", overview.rsf.getRsfh().toString()},
						new String[] {"Nb lignes I", overview.rsf.getRsfi().toString()},
						new String[] {"Nb lignes L", overview.rsf.getRsfl().toString()},
						new String[] {"Nb lignes M", overview.rsf.getRsfm().toString()},
				});
				headerlayout.addComponent(rsfPanel);
				
				// RSS PANEL
				Panel rssPanel;
				if (model.getRssoid() == null) {
					// THERE IS NO RSS FILE
					rssPanel = createPanel("Absence de RSS", new String[][] {});
				} else {
					// FILLS THE RSS CONTENT
					rssPanel = createPanel("RSS", new String[][]{
							new String[] {"Nb lignes", overview.rss.getMain().toString()},
							new String[] {"Nb actes", overview.rss.getActe().toString()},
							new String[] {"Nb diagnostics associés", overview.rss.getDa().toString()},
							new String[] {"Nb diagnostics documentaires", overview.rss.getDad().toString()},
							new String[] {"Nb séances", overview.rss.getSeances().toString()}
					});
				}
				headerlayout.addComponent(rssPanel);
				
				setVisible(true);
			} catch (ActionException e) {
				Notification.show("Erreur lors de la récupération des éléments des rsf et rss ", Notification.Type.WARNING_MESSAGE);
			}
		}
	}
	
	private Panel createPanel(String header, String[][] elements) {
		Panel panel = new Panel();
		panel.addStyleName("pims-contentpanel-headerpanel");
		VerticalLayout layout = new VerticalLayout();
		layout.addStyleName("pims-contentpanel-headerpanel-layout");
		panel.setContent(layout);

		// TITLE
		Label title = new Label(header);
		title.addStyleName("pims-contentpanel-headerpanel-headerlabel");
		layout.addComponent(title);
		
		// CONTENT
		for (String[] element : elements) {
			Label label = new Label(element[0] + " : " + element[1]);
			layout.addComponent(label);
		}
		return panel;
	}
	
}
