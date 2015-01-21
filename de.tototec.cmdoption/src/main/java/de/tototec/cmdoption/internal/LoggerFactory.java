package de.tototec.cmdoption.internal;

public class LoggerFactory {

	/**
	 * All log messages will be pass through SLF4j as-is.
	 */
	public static class Slf4jLogger implements Logger {
		private final org.slf4j.Logger underlying;

		public Slf4jLogger(final Class<?> clazz) {
			underlying = org.slf4j.LoggerFactory.getLogger(clazz);
		}

		public boolean isErrorEnabled() {
			return underlying.isErrorEnabled();
		}

		public boolean isWarnEnabled() {
			return underlying.isWarnEnabled();
		}

		public boolean isInfoEnabled() {
			return underlying.isInfoEnabled();
		}

		public boolean isDebugEnabled() {
			return underlying.isDebugEnabled();
		}

		public boolean isTraceEnabled() {
			return underlying.isTraceEnabled();
		}

		public void error(final String msg, final Object... args) {
			underlying.error(msg, args);
		}

		public void warn(final String msg, final Object... args) {
			underlying.warn(msg, args);
		}

		public void info(final String msg, final Object... args) {
			underlying.info(msg, args);
		}

		public void debug(final String msg, final Object... args) {
			underlying.debug(msg, args);
		}

		public void trace(final String msg, final Object... args) {
			underlying.trace(msg, args);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + underlying + ")";
		}
	}

	/**
	 * All log messages will be pre-processed before given to the JUL logger to
	 * support SLF4J-style placeholders and arguments semantics.
	 */
	public static class JavaUtilLogger implements Logger {
		private final java.util.logging.Logger underlying;

		public JavaUtilLogger(final Class<?> clazz) {
			underlying = java.util.logging.Logger.getLogger(clazz.getName());
		}

		protected Object[] formattedMsgAndCause(final String msg, final Object... args) {
			final String formattedMsg;
			final Throwable thrown;
			if (args != null && args.length > 0) {
				final int l;
				if (args[args.length - 1] instanceof Throwable) {
					l = args.length - 1;
					thrown = (Throwable) args[args.length - 1];
				}
				else {
					l = args.length;
					thrown = null;
				}
				if (l > 0) {
					final StringBuilder strBuilder = new StringBuilder();

					boolean braceOpen = false;
					int argNr = 0;
					for (int i = 0; i < msg.length(); ++i) {
						final char charAt = msg.charAt(i);
						if (argNr < l) {
							if (braceOpen) {
								braceOpen = false;
								if (charAt == '}') {
									// instead, insert next arg
									strBuilder.append(args[argNr]);
									++argNr;
									continue;
								} else {
									strBuilder.append("{");
								}
							} else if (charAt == '{') {
								braceOpen = true;
								continue;
							}
						}
						strBuilder.append(charAt);
					}
					if (braceOpen) {
						strBuilder.append("{");
					}

					formattedMsg = strBuilder.toString();
				} else {
					formattedMsg = msg;
				}
			} else {
				formattedMsg = msg;
				thrown = null;
			}
			return new Object[] { formattedMsg, thrown };
		}

		private void log(final java.util.logging.Level level, final String msg, final Object... args) {
			if (underlying.isLoggable(level)) {
				final Object[] formattedMsgAndCause = formattedMsgAndCause(msg, args);
				final String formattedMsg = (String) formattedMsgAndCause[0];
				final Throwable thrown = (Throwable) formattedMsgAndCause[1];
				underlying.log(level, formattedMsg, thrown);
			}

		}

		public boolean isErrorEnabled() {
			return underlying.isLoggable(java.util.logging.Level.SEVERE);
		}

		public boolean isWarnEnabled() {
			return underlying.isLoggable(java.util.logging.Level.WARNING);
		}

		public boolean isInfoEnabled() {
			return underlying.isLoggable(java.util.logging.Level.INFO);
		}

		public boolean isDebugEnabled() {
			return underlying.isLoggable(java.util.logging.Level.FINE);
		}

		public boolean isTraceEnabled() {
			return underlying.isLoggable(java.util.logging.Level.FINER);
		}

		public void error(final String msg, final Object... args) {
			log(java.util.logging.Level.SEVERE, msg, args);
		}

		public void warn(final String msg, final Object... args) {
			log(java.util.logging.Level.WARNING, msg, args);
		}

		public void info(final String msg, final Object... args) {
			log(java.util.logging.Level.INFO, msg, args);
		}

		public void debug(final String msg, final Object... args) {
			log(java.util.logging.Level.FINE, msg, args);
		}

		public void trace(final String msg, final Object... args) {
			log(java.util.logging.Level.FINER, msg, args);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "(" + underlying + ")";
		}
	}

	public static class DummyLogger implements Logger {

		public boolean isErrorEnabled() {
			return false;
		}

		public boolean isWarnEnabled() {
			return false;
		}

		public boolean isInfoEnabled() {
			return false;
		}

		public boolean isDebugEnabled() {
			return false;
		}

		public boolean isTraceEnabled() {
			return false;
		}

		public void error(final String msg, final Object... args) {}

		public void warn(final String msg, final Object... args) {}

		public void info(final String msg, final Object... args) {}

		public void debug(final String msg, final Object... args) {}

		public void trace(final String msg, final Object... args) {}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	private static transient volatile boolean slf4ClassTestedButUnavailable = false;

	public static Logger getLogger(final Class<?> clazz) {
		// synchronization would be too costly.
		// There is no problem in trying multiple times before giving up.
		if (!slf4ClassTestedButUnavailable) {
			try {
				return new Slf4jLogger(clazz);
			} catch (final NoClassDefFoundError e) {
				slf4ClassTestedButUnavailable = true;
			}
		}
		return new JavaUtilLogger(clazz);
	}
}
