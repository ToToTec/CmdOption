package de.tobiasroeser.cmdoption;

public class CmdOptionParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CmdOptionParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public CmdOptionParseException(String message) {
		super(message);
	}

}
