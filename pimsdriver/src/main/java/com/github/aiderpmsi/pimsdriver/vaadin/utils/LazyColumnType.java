package com.github.aiderpmsi.pimsdriver.vaadin.utils;

import com.vaadin.ui.Table.Align;

public class LazyColumnType {

	public String id;
	public Class<?> clazz;
	public String name;
	public Align align;

	public LazyColumnType(String id, Class<?> clazz, String name, Align align) {
		this.id = id;
		this.clazz = clazz;
		this.name = name;
		this.align = align;
	}

}
