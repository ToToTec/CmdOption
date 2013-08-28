package de.tototec.cmdoption.handler;

import java.text.MessageFormat;

import de.tototec.cmdoption.CmdlineParserException;
import de.tototec.cmdoption.I18nFactory;

public class CmdOptionHandlerException extends Exception {

	private static final long serialVersionUID = 1L;
	private String localizedMessage;

	public CmdOptionHandlerException(final String message, final Throwable cause, final String localizedMessage) {
		super(message, cause);
		this.localizedMessage = localizedMessage;
	}

	public CmdOptionHandlerException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CmdOptionHandlerException(final String message, final String localizedMessage) {
		super(message);
		this.localizedMessage = localizedMessage;
	}

	public CmdOptionHandlerException(final String message) {
		super(message);
	}

	public CmdOptionHandlerException(final Throwable cause, final String message, final Object... params) {
		this(params == null || params.length == 0 ? message : MessageFormat.format(message, params), cause, I18nFactory
				.getI18n(CmdlineParserException.class).tr(message, params));
	}


	@Override
	public String getLocalizedMessage() {
		return localizedMessage == null ? getMessage() : localizedMessage;
	}
}
