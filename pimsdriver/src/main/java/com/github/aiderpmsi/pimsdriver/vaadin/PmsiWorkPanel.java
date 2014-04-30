package com.github.aiderpmsi.pimsdriver.vaadin;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class PmsiWorkPanel extends Panel {

	/** Generated serial id */
	private static final long serialVersionUID = 9173237483341882407L;

	public void setUpload(Long recordId) {
		// IF RECORDID IS NULL, WE HAVE TO REMOVE EVERYTHIN OF THIS PANEL
		if (recordId == null) {
			this.removeAllActionHandlers();
			this.setContent(new VerticalLayout());

		}
		else {
			// GETS THE DATAS TO WRITE
			NavigationDTO.RsfSynthesis rsfs = (new NavigationDTO()).rsfSynthesis(recordId);
			NavigationDTO.RssSynthesis rsss =  (new NavigationDTO()).rssSynthesis(recordId);
			
			// SETS THE LAYOUT
			VerticalLayout principallayout = new VerticalLayout();
			setContent(principallayout);
			
			GridLayout headerlayout = new GridLayout(2, 1);
			principallayout.addComponent(headerlayout);
			
			// RSF PANEL
			Panel rsfPanel = new Panel();
			headerlayout.addComponent(rsfPanel);
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
	
			// RSS PANEL
			Panel rssPanel = new Panel();
			headerlayout.addComponent(rssPanel);
			VerticalLayout rssLayout = new VerticalLayout();
			rsfPanel.setContent(rssLayout);
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
		}
	}
	
}
