package com.github.aiderpmsi.pimsdriver.vaadin;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class PmsiContentPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;

	public void setUpload(PmsiUploadedElementModel model, PmsiUploadedElementModel.Status status) {
		// IF STATUS IS FAILED, WE HAVE TO REMOVE EVERYTHIN OF THIS PANEL
		if (status == PmsiUploadedElementModel.Status.failed) {
			this.removeAllActionHandlers();
			this.setContent(new VerticalLayout());
		}
		else {
			// GETS THE DATAS TO WRITE
			NavigationDTO.RsfSynthesis rsfs = (new NavigationDTO()).rsfSynthesis(model.getRecordId());
			NavigationDTO.RssSynthesis rsss =  (new NavigationDTO()).rssSynthesis(model.getRecordId());
			
			// SETS THE LAYOUT
			VerticalLayout principallayout = new VerticalLayout();
			setContent(principallayout);
			
			HorizontalLayout headerlayout = new HorizontalLayout();
			principallayout.addComponent(headerlayout);
			
			// RSF PANEL
			Panel rsfPanel = new Panel();
			VerticalLayout rsfLayout = new VerticalLayout();
			rsfPanel.setContent(rsfLayout);
			
			Label rsfHeader = new Label("RSF");
			rsfLayout.addComponent(rsfHeader);
			Label rsfa = new Label("Nb lignes A : " + rsfs.rsfa);
			Label rsfb = new Label("Nb lignes B : " + rsfs.rsfb);
			Label rsfc = new Label("Nb lignes C : " + rsfs.rsfc);
			Label rsfh = new Label("Nb lignes H : " + rsfs.rsfh);
			Label rsfi = new Label("Nb lignes I : " + rsfs.rsfi);
			Label rsfl = new Label("Nb lignes L : " + rsfs.rsfa);
			Label rsfm = new Label("Nb lignes M : " + rsfs.rsfa);
			
			rsfLayout.addComponents(rsfa, rsfb, rsfc, rsfh, rsfi, rsfl, rsfm);

			headerlayout.addComponent(rsfPanel);
			
			// RSS PANEL
			Panel rssPanel = new Panel();
			VerticalLayout rssLayout = new VerticalLayout();
			rssPanel.setContent(rssLayout);
			// NO RSS
			if (rsss == null) {
				Label rssHeader = new Label("Absence de RSS");
				rssLayout.addComponent(rssHeader);
			}
			// ONE RSS
			else {
				Label rssHeader = new Label("RSS");
				rssLayout.addComponent(rssHeader);
				Label rsssmain = new Label("Nb lignes : " + rsss.main);
				Label rsssacte = new Label("Nb d'actes : " + rsss.acte);
				Label rsssda = new Label("Nb de diagnostics : " + rsss.da);
				Label rssdad = new Label("Nb de diagnostics documentaires : " + rsss.dad);
				Label rssseances = new Label("Nb de séances : " + rsss.seances);
				rssLayout.addComponents(rsssmain, rsssacte, rsssda, rssdad, rssseances);
			}
			headerlayout.addComponent(rssPanel);
		}
	}
	
}
