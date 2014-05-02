package com.github.aiderpmsi.pimsdriver.vaadin.main.finesspanel;

import java.text.SimpleDateFormat;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dao.NavigationDTO;
import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;

public class ExpandListener implements Tree.ExpandListener {

	private static final long serialVersionUID = 8913677773696542760L;

	private HierarchicalContainer hc;
	
	private FinessPanel fp;
	
	private NavigationDTO navigationDTO;
	
	@SuppressWarnings("unused")
	private ExpandListener() {}
	
	public ExpandListener(HierarchicalContainer hc, FinessPanel fp, NavigationDTO navigationDTO) {
		this.hc = hc;
		this.navigationDTO = navigationDTO;
		this.fp = fp;
	}
	
	private Object createNode(Object[][] elements) {
		Object id = hc.addItem();
		for (Object[] element : elements) {
			@SuppressWarnings("unchecked")
			Property<Object> prop = (Property<Object>) hc.getContainerProperty(id, element[0]);
			prop.setValue(element[1]);
		}
		return id;
	}
	
	@Override
	public synchronized void nodeExpand(ExpandEvent event) {

		// GETS THE EVENT NODE DEPTH
		Integer eventDepth =
				(Integer) hc.getContainerProperty(
						event.getItemId(), "depth").getValue();
		// GETS THE STATUS OF THE NODE (SUCCESSED OR FAILED)
		PmsiUploadedElementModel.Status eventStatus =
				(PmsiUploadedElementModel.Status) hc.getContainerProperty(
						event.getItemId(), "status").getValue();
		
		// IF WE EXPAND A ROOT NODE
		if (eventDepth == 0) {
				// FILL THE CORRESPONDING TREE
				List<String> finesses = navigationDTO.getFiness(eventStatus);
				for (String finess : finesses) {
					// CREATES THE NODE
					Object id = createNode(new Object[][] {
							new Object[] {"caption", finess},
							new Object[] {"finess", finess},
							new Object[] {"depth", new Integer(1)},
							new Object[] {"status", eventStatus}
					});
					// ATTACHES THE NODE
					hc.setParent(id, event.getItemId());
				}
			}

		// IF WE EXPAND A FINESS NODE
		else if (eventDepth == 1) {
			// GETS THE FINESS
			String finess = (String) hc.getContainerProperty(event.getItemId(), "finess").getValue();

			// FILL THE FINESS TREE :
			List<NavigationDTO.YM> yms = (new NavigationDTO()).getYM(eventStatus, finess);

			// WHEN YMS IS NULL, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
			if (yms == null) {
				fp.removeItem(event.getItemId());
				// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
				Notification.show("Le finess sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
			} else {
				for (NavigationDTO.YM ym : yms) {
					// CREATES THE NODE
					Object id = createNode(new Object[][] {
							new Object[] {"caption", ym.year + " M" + ym.month},
							new Object[] {"year", ym.year},
							new Object[] {"month", ym.month},
							new Object[] {"finess", finess},
							new Object[] {"status", eventStatus},
							new Object[] {"depth", new Integer(2)}
					});
					// ATTACHES THE NODE
					hc.setParent(id, event.getItemId());
				}
			}
		}
		
		// IF WE EXPAND A YEAR / MONTH NODE
		else if (eventDepth == 2) {
			String finess = (String) hc.getContainerProperty(event.getItemId(), "finess").getValue();
			Integer year = (Integer) hc.getContainerProperty(event.getItemId(), "year").getValue();
			Integer month = (Integer) hc.getContainerProperty(event.getItemId(), "month").getValue();
			
			// CREATES THE QUERY TO GET THE UPLOADED ITEMS
			UploadedElementsDTO ued = new UploadedElementsDTO();
			List<PmsiUploadedElementModel> models = 
					ued.getUploadedElements(
							"SELECT plud_id, plud_processed, plud_finess, plud_year, plud_month, plud_dateenvoi FROM plud_pmsiupload WHERE plud_processed = ?::plud_status AND plud_finess = ? AND plud_year = ? AND plud_month = ? ORDER BY plud_dateenvoi DESC",
							new Object[] {eventStatus.toString(), finess, year, month});

			// IF WE HAVE NO RESULT, IT MEANS THIS ITEM DOESN'T EXIST ANYMORE, REMOVE IT FROM THE TREE
			if (models.size() == 0) {
				fp.removeItem(event.getItemId());
				// SHOW THAT THIS ITEM DOESN'T EXIST ANYMORE
				Notification.show("L'élément sélectionné n'existe plus", Notification.Type.WARNING_MESSAGE);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
				for (PmsiUploadedElementModel model : models) {
					// CREATES THE NODE
					Object id = createNode(new Object[][] {
							new Object[] {"caption", sdf.format(model.getDateenvoi())},
							new Object[] {"model", model},
							new Object[] {"year", year},
							new Object[] {"month", month},
							new Object[] {"finess", finess},
							new Object[] {"status", eventStatus},
							new Object[] {"depth", new Integer(3)}
					});
					// ATTACHES THE NODE
					hc.setParent(id, event.getItemId());
					hc.setChildrenAllowed(id, false);
					
					
				}
			}
		}
	}
}
