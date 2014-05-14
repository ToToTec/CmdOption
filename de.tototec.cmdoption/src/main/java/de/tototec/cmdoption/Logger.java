package de.tototec.cmdoption;

/**
 * A lightweight wrapper around a logging API, if available.
 * 
 * We only support methods with Java varargs and expect, that logging calls are
 * not time critical (which should be ok in most cases where you want to use a
 * commandline parser).
 * 
 */
public interface Logger {

	boolean isErrorEnabled();

	boolean isWarnEnabled();

	boolean isInfoEnabled();

	boolean isDebugEnabled();

	boolean isTraceEnabled();

	void error(String msg, Object... args);

	void warn(String msg, Object... args);

	void info(String msg, Object... args);

	void debug(String msg, Object... args);

	void trace(String msg, Object... args);

}
