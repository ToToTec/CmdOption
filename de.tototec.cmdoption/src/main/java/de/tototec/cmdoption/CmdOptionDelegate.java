package de.tototec.cmdoption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Also inspect the object stored in the annotated field for {@link CmdOption}
 * annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface CmdOptionDelegate {

	enum Mode {
		/** Scan for options. */
		OPTIONS,
		/** Scan for command. */
		COMMAND,
		/** Scan for command and options. */
		COMMAND_OR_OPTIONS
	}

	/**
	 * The mode to configure how {@link CmdCommand}-annotated classes should be handled.
	 * @since CmdOption 0.7.0
	 */
	Mode value() default Mode.OPTIONS;
}
