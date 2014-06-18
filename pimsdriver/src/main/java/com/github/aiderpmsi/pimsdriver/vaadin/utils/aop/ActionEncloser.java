package com.github.aiderpmsi.pimsdriver.vaadin.utils.aop;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.vaadin.ui.Notification;

public abstract class ActionEncloser {

	public interface ActionExecuter {
		public void action() throws ActionException;
		public String msgError(ActionException e);
	}
	
	public interface ActionReturner<R>  {
		public R action() throws ActionException;
		public String msgError(ActionException e);
	}
	
	public static void execute(ActionExecuter executer) {
		try {
			executer.action();
		} catch (ActionException e) {
			Notification.show(executer.msgError(e), Notification.Type.WARNING_MESSAGE);
		}
	}
	
	public static <R> R executer(ActionReturner<R> executer) {
		try {
			return executer.action();
		} catch (ActionException e) {
			Notification.show(executer.msgError(e), Notification.Type.WARNING_MESSAGE);
		}
		return null;
	}

}
