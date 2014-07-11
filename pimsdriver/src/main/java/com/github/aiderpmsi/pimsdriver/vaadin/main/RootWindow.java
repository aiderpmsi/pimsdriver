package com.github.aiderpmsi.pimsdriver.vaadin.main;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("pimsdriver")
public class RootWindow extends UI {

	/** Generated Serial */
	private static final long serialVersionUID = 3109715875916629911L;
	
	@SuppressWarnings("serial")
	private final Layout layout = new VerticalLayout() {{
		addStyleName("pims-main-layout");
	}};

	@SuppressWarnings("serial")
	private final Label header = new Label() {{
		addStyleName("pims-main-header");
	}};
	
	private final MenuBar menuBar = new MenuBar(this);

	private final SplitPanel splitPanel = new SplitPanel(this);

	private final ServletContext servletContext = VaadinServlet.getCurrent().getServletContext();
	
	@Override
	protected void init(final VaadinRequest request) {
		layout.addComponent(header);
		layout.addComponent(menuBar);
		layout.addComponent(splitPanel);
		setContent(layout);
	}
		
	public void setUploadSelected(final UploadedPmsi model, final UploadedPmsi.Status status) {
		splitPanel.getContentPanel().setUpload(model, status);
		menuBar.setUpload(model, status);
	}
	
	public void setMenuNavigationSelected(final UploadedPmsi model, final MenuBar.MenuBarSelected type) {
		splitPanel.getContentPanel().show(type, model);
	}

	public Layout getLayout() {
		return layout;
	}

	public Label getHeader() {
		return header;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public SplitPanel getSplitPanel() {
		return splitPanel;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

}
