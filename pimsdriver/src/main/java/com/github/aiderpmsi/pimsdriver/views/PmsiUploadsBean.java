package com.github.aiderpmsi.pimsdriver.views;

import java.io.Serializable;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.icefaces.ace.model.table.LazyDataModel;

@Named("views.pmsiUploadsBean")
@ConversationScoped
public class PmsiUploadsBean implements Serializable {

	/**
	 * Generated serialid
	 */
	private static final long serialVersionUID = 712160288095075205L;

	// ===== Filter generated from request =====
	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	@NotNull
	private String filter;
	
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
		lazyModel = new PmsiUploadDatasLazyModel(filter);
	}
	
	// ===== Model, for reading from database =====
	private LazyDataModel<PmsiUploadElement> lazyModel;  

	public LazyDataModel<PmsiUploadElement> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<PmsiUploadElement> lazyModel) {
		this.lazyModel = lazyModel;
	}

}
