package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.vaadin.main.RootWindow;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;

public class ItemClickListener implements ItemClickEvent.ItemClickListener {

	private static final long serialVersionUID = 822165023770852409L;

	@SuppressWarnings("unused")
	private ItemClickListener() {};
	
	private RootWindow rootElement;
	
	private HierarchicalContainer hc;
	
	public ItemClickListener(RootWindow rootElement, HierarchicalContainer hc) {
		this.rootElement = rootElement;
		this.hc = hc;
	}

	@Override
	public void itemClick(ItemClickEvent event) {

		// GETS THE EVENT NODE DEPTH
		Integer eventDepth =
				(Integer) hc.getContainerProperty(
						event.getItemId(), "depth").getValue();
		// GETS THE STATUS OF THE NODE (SUCCESSED OR FAILED)
		UploadedPmsi.Status eventStatus =
				(UploadedPmsi.Status) hc.getContainerProperty(
						event.getItemId(), "status").getValue();

		// DEPTH AT 3 MEANS AN UPLOAD HAS BEEN SELECTED
		if (eventDepth == 3) {
			//  PREVENT GUIUI THAT AN UPLOAD HAS BEEN SELECTED
			rootElement.fireFinessSelected(
					(UploadedPmsi) hc.getContainerProperty(event.getItemId(), "model").getValue(),
					eventStatus);
		}
		// SOMETHING ELSE HAS BEEN SELECTED, PREVENT THE MAIN WINDOW
		else {
			rootElement.fireFinessSelected(null, null);
		}
		
	}

}
