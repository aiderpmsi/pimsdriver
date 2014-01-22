package com.github.aiderpmsi.pimsdriver.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.primefaces.component.api.UIColumn;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("views.pmsiUploadDatas")
@ConversationScoped
public class PmsiUploadDatas implements Serializable {

	/**
	 * Generated serialid
	 */
	private static final long serialVersionUID = 712160288095075205L;

	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	@NotNull
	private String filter;
	
	private LazyDataModel<PmsiUploadElement> lazyModel;  
	
	private PmsiUploadElement selectedElement;
		
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		lazyModel = new PmsiUploadDatasLazyModel(filter);
	}

	public LazyDataModel<PmsiUploadElement> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<PmsiUploadElement> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public PmsiUploadElement getSelectedElement() {
		return selectedElement;
	}

	public void setSelectedElement(PmsiUploadElement selectedElement) {
		this.selectedElement = selectedElement;
	}

	public List<SortMeta> getInitialSort() {
		List<SortMeta> initialSort = new ArrayList<>(1);

		UIViewRoot viewRoot =  FacesContext.getCurrentInstance().getViewRoot();
		UIComponent column = viewRoot.findComponent("uploadedform:elementTable:finessValue"); 

		SortMeta sm1 = new SortMeta();	
		sm1.setSortBy((UIColumn)column);
		sm1.setSortField("finessValue");
		sm1.setSortOrder(SortOrder.DESCENDING);
		initialSort.add(sm1);
		
		return initialSort;
	}

}
