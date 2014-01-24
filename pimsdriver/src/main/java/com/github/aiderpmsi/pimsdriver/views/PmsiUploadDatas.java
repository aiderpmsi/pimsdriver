package com.github.aiderpmsi.pimsdriver.views;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ajax4jsf.model.ExtendedDataModel;

@Named("views.pmsiUploadDatas")
@SessionScoped
public class PmsiUploadDatas implements Serializable {

	/**
	 * Generated serialid
	 */
	private static final long serialVersionUID = 712160288095075205L;

	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	@NotNull
	private String filter;
	
	private ExtendedDataModel<PmsiUploadElement> lazyModel;  
	
	private PmsiUploadElement selectedElement;
		
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		lazyModel = new PmsiUploadDatasLazyModel(filter);
	}

	public ExtendedDataModel<PmsiUploadElement> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(ExtendedDataModel<PmsiUploadElement> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public PmsiUploadElement getSelectedElement() {
		return selectedElement;
	}

	public void setSelectedElement(PmsiUploadElement selectedElement) {
		this.selectedElement = selectedElement;
	}

}
