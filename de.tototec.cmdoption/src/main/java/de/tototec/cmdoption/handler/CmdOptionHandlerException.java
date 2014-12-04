package de.tototec.cmdoption.handler;

public class CmdOptionHandlerException extends Exception {

	private static final long serialVersionUID = 1L;
	private String localizedMessage;

	public CmdOptionHandlerException(final String message, final Throwable cause, final String localizedMessage) {
		super(message, cause);
		this.localizedMessage = localizedMessage;
	}

	public CmdOptionHandlerException(final String message, final String localizedMessage) {
		this(message, null, localizedMessage);
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage == null ? getMessage() : localizedMessage;
	}
}
