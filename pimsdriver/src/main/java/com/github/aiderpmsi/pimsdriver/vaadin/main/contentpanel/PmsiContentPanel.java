package com.github.aiderpmsi.pimsdriver.vaadin.main.contentpanel;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
			// GETS THE DATAS TO WRITE
			NavigationDTO.RsfOverview rsfs = (new NavigationDTO()).rsfSynthesis(model.getRecordid());
			NavigationDTO.RssOverview rsss =  (new NavigationDTO()).rssSynthesis(model.getRecordid());
			
			// SETS THE LAYOUT
			VerticalLayout principallayout = new VerticalLayout();
			setContent(principallayout);
			
			HorizontalLayout headerlayout = new HorizontalLayout();
			principallayout.addStyleName("pims-contentpanel-headerlayout");
			principallayout.addComponent(headerlayout);
			
			// RSF PANEL
			Panel rsfPanel = createPanel("RSF", new String[][] {
					new String[] {"Nb lignes A", rsfs.rsfa.toString()},
					new String[] {"Nb lignes B", rsfs.rsfb.toString()},
					new String[] {"Nb lignes C", rsfs.rsfc.toString()},
					new String[] {"Nb lignes H", rsfs.rsfh.toString()},
					new String[] {"Nb lignes I", rsfs.rsfi.toString()},
					new String[] {"Nb lignes L", rsfs.rsfl.toString()},
					new String[] {"Nb lignes M", rsfs.rsfm.toString()},
			});
			headerlayout.addComponent(rsfPanel);
			
			// RSS PANEL
			Panel rssPanel;
			if (rsss == null) {
				// THERE IS NO RSS FILE
				rssPanel = createPanel("Absence de RSS", new String[][] {});
			} else {
				// FILLS THE RSS CONTENT
				rssPanel = createPanel("RSS", new String[][]{
						new String[] {"Nb lignes", rsss.main.toString()},
						new String[] {"Nb actes", rsss.acte.toString()},
						new String[] {"Nb diagnostics associés", rsss.da.toString()},
						new String[] {"Nb diagnostics documentaires", rsss.dad.toString()},
						new String[] {"Nb séances", rsss.seances.toString()}
				});
			}
			headerlayout.addComponent(rssPanel);
			
			setVisible(true);
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
