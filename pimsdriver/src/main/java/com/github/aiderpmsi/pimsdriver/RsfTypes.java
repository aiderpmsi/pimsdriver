package com.github.aiderpmsi.pimsdriver;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

@Named("rsfTypes")
@ApplicationScoped
public class RsfTypes {

	private final static String[] types = new String[] {
		"rsf2009", "rsf2012"
	};
	
	private List<SelectItem> items = new ArrayList<SelectItem>(types.length) {
		private static final long serialVersionUID = 981852153158891261L;
		{
			for (int i = 0 ; i < types.length ; i++) {
				add(new SelectItem(i, types[i]));
			}
		}
	};
	
	public List<SelectItem> getItems() {
		return items;
	}

}
