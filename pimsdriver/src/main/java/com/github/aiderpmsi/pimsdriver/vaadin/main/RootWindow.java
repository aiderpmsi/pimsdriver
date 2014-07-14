package com.github.aiderpmsi.pimsdriver.vaadin.main;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

@Theme("pimsdriver")
public class RootWindow extends UI {

	/** Generated Serial */
	private static final long serialVersionUID = 3109715875916629911L;
	
	private CssLayout layout;

	private Label header;
	
	private MenuBar menuBar;

	private SplitPanel splitPanel;

	private ServletContext servletContext;
	
	@Override
	protected void init(final VaadinRequest request) {
		
		servletContext = VaadinServlet.getCurrent().getServletContext();

		layout = new CssLayout();
		layout.addStyleName("pims-main-layout");
		header = new Label("pimsdriver");
		header.addStyleName("pims-main-header");
		menuBar = new MenuBar(this);
		splitPanel = new SplitPanel(this);
		
		layout.addComponent(header);
		layout.addComponent(menuBar);
		layout.addComponent(splitPanel);
		setContent(layout);
	}
		
	public void setUploadSelected(final UploadedPmsi model) {
		splitPanel.getContentPanel().setUpload(model);
		menuBar.setUpload(model);
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
