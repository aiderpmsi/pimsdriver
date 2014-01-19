package com.github.aiderpmsi.pimsdriver.defs;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

@Named("defs.monthValues")
@ApplicationScoped
public class Months {

	private final static String[] months = new String[] {
		"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
		"Juillet", "Août", "Septembre", "Octobre", "Novembre", "Decembre"
	};
	
	private List<SelectItem> items = new ArrayList<SelectItem>(12) {
		private static final long serialVersionUID = -8154250841667407740L;
		{
			for (int i = 0 ; i < 12 ; i++) {
				add(new SelectItem(i, months[i]));
			}
		}
	};
	
	public List<SelectItem> getItems() {
		return items;
	}

}
