package org.bukkitmodders.copycat.plugin;

public class NeedMoreArgumentsException extends Exception {

	private static final long serialVersionUID = 7149223696312513389L;

	public NeedMoreArgumentsException(String message, Throwable cause) {
		super(message, cause);
	}

	public NeedMoreArgumentsException(String message) {
		super(message);
	}

	public NeedMoreArgumentsException(Throwable cause) {
		super(cause);
	}
}
