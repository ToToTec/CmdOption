package de.tototec.cmdoption;

import java.text.MessageFormat;

import de.tototec.cmdoption.internal.I18nFactory;

public class CmdlineParserException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String localizedMessage;

	public CmdlineParserException(final String message, final Throwable cause, final String localizedMessage) {
		super(message, cause);
		this.localizedMessage = localizedMessage;
	}

	public CmdlineParserException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CmdlineParserException(final String message, final String localizedMessage) {
		super(message);
		this.localizedMessage = localizedMessage;
	}

	public CmdlineParserException(final String message) {
		super(message);
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage == null ? getMessage() : localizedMessage;
	}

}
