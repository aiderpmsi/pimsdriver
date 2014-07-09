package com.github.aiderpmsi.pimsdriver.vaadin.utils.aop;

import com.vaadin.ui.Component;

public class ListenerEncloser<T> implements Component.Listener {

	/** Generated serial id */
	private static final long serialVersionUID = -5268198876163329668L;

	private final Class<T> filter;
	
	private final Listener<T> listener;
	
	public ListenerEncloser(Class<T> filter, Listener<T> listener) {
		this.filter = filter;
		this.listener = listener;
	}

	@Override
	public void componentEvent(Component.Event event) {
		if (filter.isAssignableFrom(event.getClass())) {
			@SuppressWarnings("unchecked")
			T castedEvent = (T) event;
			listener.componentEvent(castedEvent);
		}
	}
	
	public interface Listener<T> {
		public void componentEvent(T event);
	}
	
}
