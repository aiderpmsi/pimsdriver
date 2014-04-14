package com.github.aiderpmsi.pimsdriver.vaadin;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PmsiProcessWindow extends Window {

	/** Generated serial Id */
	private static final long serialVersionUID = -7803472921198470202L;

	public PmsiProcessWindow() {
		// TITLE
		super("Fichiers pmsi en cours de traitement");
		
		// SET VISUAL ASPECT
        setWidth("650px");
        setClosable(true);
        setResizable(true);
        setModal(true);
        setStyleName("processpmsi");
        center();

        // SELECT LAYOUT
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);

        // ADDS DATATABLE
        Table processtable = new Table();
        processtable.setContainerDataSource(
        		new LazyQueryContainer(
        				new LazyQueryDefinition(false, 1000, "RID"),
        				new PmsiProcessQueryFactory()));
        processtable.setVisibleColumns(new Object[] { "finess", "monthValue", "yearValue" });
        processtable.setSelectable(true);
        processtable.setImmediate(true);
        layout.addComponent(processtable);
	}

}
