package com.github.aiderpmsi.pimsdriver.vaadin.utils.aop;

import java.util.function.Function;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.vaadin.ui.Notification;

public class ActionEncloser<R> {

	public static void executeVoid(final Function<ActionException, String> errorMsgSupplier,
			final ExecuterVoid actionSupplier) {
		try {
			actionSupplier.execute();
		} catch (ActionException e) {
			showError(errorMsgSupplier.apply(e));
		}
	}

	@FunctionalInterface
	public interface ExecuterVoid {
		public void execute() throws ActionException;
	}

	public static <R> R execute(final Function<ActionException, String> errorMsgSupplier,
			final Executer<R> actionSupplier) {
		try {
			return actionSupplier.execute();
		} catch (ActionException e) {
			showError(errorMsgSupplier.apply(e));
		}
		return null;
	}

	@FunctionalInterface
	public interface Executer<R> {
		public R execute() throws ActionException;
	}
	
	private static void showError(final String message) {
		Notification.show(message, Notification.Type.WARNING_MESSAGE);
	}
}
