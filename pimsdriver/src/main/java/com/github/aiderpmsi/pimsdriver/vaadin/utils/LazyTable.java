package com.github.aiderpmsi.pimsdriver.vaadin.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Table;

public class LazyTable extends Table {

	/** Generated id */
	private static final long serialVersionUID = -8031105462273795086L;
	
	public LazyTable(LazyColumnType[] columns, Locale locale, LazyQueryContainer c) {
		setLocale(locale);
		
		List<String> visibleColumns = new LinkedList<>();
		List<String> columnHeaders = new LinkedList<>();
		List<Align> columnAlignements = new LinkedList<>();
		
		for (LazyColumnType column : columns) {
			c.addContainerProperty(column.id, column.clazz, null, true, true);
			if (column.name != null) {
				visibleColumns.add(column.id);
				columnHeaders.add(column.name);
				columnAlignements.add(column.align);
			}
		}

		setContainerDataSource(c);
		setVisibleColumns(visibleColumns.toArray());
		setColumnHeaders(columnHeaders.toArray(new String[columnHeaders.size()]));
		setColumnAlignments(columnAlignements.toArray(new Align[columnAlignements.size()]));
	}

}
