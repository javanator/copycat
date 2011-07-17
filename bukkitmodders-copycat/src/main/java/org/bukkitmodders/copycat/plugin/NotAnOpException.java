package org.bukkitmodders.copycat.plugin;

public class NotAnOpException extends Exception {

	public NotAnOpException() {
		super();
	}

	public NotAnOpException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAnOpException(String message) {
		super(message);
	}

	public NotAnOpException(Throwable cause) {
		super(cause);
	}
}
