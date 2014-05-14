package de.tototec.cmdoption;

public class LoggerFactory {

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

		public void error(final String msg, final Object... args) {
		}

		public void warn(final String msg, final Object... args) {
		}

		public void info(final String msg, final Object... args) {
		}

		public void debug(final String msg, final Object... args) {
		}

		public void trace(final String msg, final Object... args) {
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	private static Logger dummyLogger = null;

	public static Logger getLogger(final Class<?> clazz) {
		if (dummyLogger != null)
			return dummyLogger;
		try {
			return new Slf4jLogger(clazz);
		} catch (final NoClassDefFoundError e) {
			if (dummyLogger == null) {
				synchronized (LoggerFactory.class) {
					if (dummyLogger == null) {
						dummyLogger = new DummyLogger();
					}
				}
			}
			return dummyLogger;
		}
	}
}
