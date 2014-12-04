package de.tototec.cmdoption;

public class CmdlineParserException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String localizedMessage;

	public CmdlineParserException(final String message, final Throwable cause, final String localizedMessage) {
		super(message, cause);
		this.localizedMessage = localizedMessage;
	}

	public CmdlineParserException(final String message, final String localizedMessage) {
		this(message, null, localizedMessage);
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage == null ? getMessage() : localizedMessage;
	}

}
