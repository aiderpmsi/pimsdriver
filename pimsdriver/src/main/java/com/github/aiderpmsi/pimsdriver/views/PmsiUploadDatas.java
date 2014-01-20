package com.github.aiderpmsi.pimsdriver.views;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;

import org.primefaces.model.LazyDataModel;

@Named("views.pmsiUploadDatas")
@SessionScoped
public class PmsiUploadDatas {

	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	private String filter;
	
	private LazyDataModel<PmsiUploadElement> lazyModel;  
	
	private PmsiUploadElement selectedElement;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
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
	
	@PostConstruct
	public void init() {
		lazyModel = new PmsiUploadDatasLazyModel(filter);
	}
	
}
