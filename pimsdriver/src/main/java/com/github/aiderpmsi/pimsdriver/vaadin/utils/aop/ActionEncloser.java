package com.github.aiderpmsi.pimsdriver.vaadin.utils.aop;

import java.util.function.Function;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.vaadin.ui.Notification;

public class ActionEncloser<R> {

	public static <R> R execute(final Function<ActionException, String> errorMsgSupplier,
			final Executer<R> actionSupplier) {
		try {
			return actionSupplier.execute();
		} catch (ActionException e) {
			Notification.show(errorMsgSupplier.apply(e), Notification.Type.WARNING_MESSAGE);
		}
		return null;
	}

	@FunctionalInterface
	public interface Executer<R> {
		public R execute() throws ActionException;
	}
}
