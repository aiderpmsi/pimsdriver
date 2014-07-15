package com.github.aiderpmsi.pimsdriver.vaadin.main;

import javax.servlet.ServletContext;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@Theme("pimsdriver")
public class MainApplication extends UI {

	/** Generated Serial */
	private static final long serialVersionUID = 3109715875916629911L;
	
	private RootWindow layout;

	private ServletContext servletContext;
	
	@Override
	protected void init(final VaadinRequest request) {
		servletContext = VaadinServlet.getCurrent().getServletContext();

		layout = new RootWindow(this);
		layout.addStyleName("pims-main-layout");
		setContent(layout);

	}

	public ServletContext getServletContext() {
		return servletContext;
	}

}
