package com.github.aiderpmsi.pimsdriver.vaadin.utils.aop;

import java.util.function.Function;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.vaadin.ui.Notification;

public class ActionEncloser<R, S> {

	private final Function<ActionException, String> errorMsgSupplier;
	
	private final Executer<R, S> actionSupplier;
	
	private final S actionsProvider;
	
	public ActionEncloser(
			final S actionsProvider,
			final Function<ActionException, String> errorMsgSupplier,
			final Executer<R, S> actionSupplier) {
		this.actionsProvider = actionsProvider;
		this.errorMsgSupplier = errorMsgSupplier;
		this.actionSupplier = actionSupplier;
	}
	
	public R execute() {
		try {
			return actionSupplier.execute(actionsProvider);
		} catch (ActionException e) {
			Notification.show(errorMsgSupplier.apply(e), Notification.Type.WARNING_MESSAGE);
		}
		return null;
	}

	@FunctionalInterface
	public interface Executer<R, S> {
		public R execute(S actionsProvider) throws ActionException;
	}
}
