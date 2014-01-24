package com.github.aiderpmsi.pimsdriver.views;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.ajax4jsf.model.ExtendedDataModel;
import org.richfaces.component.SortOrder;

@Named("views.pmsiUploadsBean")
@SessionScoped
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
	private ExtendedDataModel<PmsiUploadElement> lazyModel;  

	public ExtendedDataModel<PmsiUploadElement> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(ExtendedDataModel<PmsiUploadElement> lazyModel) {
		this.lazyModel = lazyModel;
	}

	// ===== Sorting and Filter ===== 
		
	private Map<String, SortOrder> sortOrders = new HashMap<>();
	
    private Map<String, String> filterValues = new HashMap<>();
    
    private String sortProperty;
    
    public PmsiUploadsBean() {
    	// sortOrders.put("name", SortOrder.unsorted);

    }

    public Map<String, SortOrder> getSortOrders() {
        return sortOrders;
    }
 
    public Map<String, String> getFilterValues() {
        return filterValues;
    }
 
    public String getSortProperty() {
        return sortProperty;
    }
 
    public void setSortProperty(String sortPropety) {
        this.sortProperty = sortPropety;
    }
 
    public void toggleSort() {
        for (Entry<String, SortOrder> entry : sortOrders.entrySet()) {
            SortOrder newOrder;
 
            if (entry.getKey().equals(sortProperty)) {
                if (entry.getValue() == SortOrder.ascending) {
                    newOrder = SortOrder.descending;
                } else {
                    newOrder = SortOrder.ascending;
                }
            } else {
                newOrder = SortOrder.unsorted;
            }
 
            entry.setValue(newOrder);
        }
    }
}
