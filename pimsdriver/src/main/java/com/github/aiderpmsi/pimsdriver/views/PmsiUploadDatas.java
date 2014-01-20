package com.github.aiderpmsi.pimsdriver.views;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;
import javax.validation.constraints.Pattern;

import org.primefaces.model.LazyDataModel;

@Named("views.pmsiUploadDatas")
@ConversationScoped
public class PmsiUploadDatas {

	@Pattern(regexp="(notprocessed)|(processed)|(all)")
	private String filter;
	
	private LazyDataModel<PmsiUploadElement> lazyModel;  
	
	

}
