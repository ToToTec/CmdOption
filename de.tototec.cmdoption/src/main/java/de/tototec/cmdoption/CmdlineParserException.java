package de.tototec.cmdoption;

public class CmdlineParserException extends RuntimeException {

	public CmdlineParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public CmdlineParserException(String message) {
		super(message);
	}

}
