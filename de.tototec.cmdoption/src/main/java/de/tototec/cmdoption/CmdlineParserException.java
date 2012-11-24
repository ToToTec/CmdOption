package de.tototec.cmdoption;

import java.text.MessageFormat;

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

	public CmdlineParserException(final Throwable cause, final String message, final Object... params) {
		this(params == null || params.length == 0 ? message : MessageFormat.format(message, params), cause, I18nFactory
				.getI18n(CmdlineParserException.class).tr(message, params));
	}

	@Override
	public String getLocalizedMessage() {
		return localizedMessage;
	}

}
