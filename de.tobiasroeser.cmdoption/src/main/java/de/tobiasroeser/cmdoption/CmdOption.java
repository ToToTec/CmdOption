package de.tobiasroeser.cmdoption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Command line option which optionally supports parameters.
 * 
 * @see Option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface CmdOption {

	/**
	 * The long option name.
	 */
	String longName() default "";

	/**
	 * A short one-letter version of the option.
	 */
	String shortName() default "";

	/**
	 * The description of the option. You can use variable substitutes (e.g.
	 * ${verbose}) to reference other options (e.g. --verbose).}.
	 */
	String description() default "";

	/**
	 * The arguments (their names) supported by this option.
	 */
	String[] args() default {};

	/**
	 * An {@link CmdOptionHandler} to apply the parsed option to the annotated
	 * field or method. If this is not given, all handler registered for
	 * auto-detect will by tried in order.
	 */
	Class<? extends CmdOptionHandler> handler() default CmdOptionHandler.class;

	/**
	 * The minimal needed count this option is required.
	 */
	int minCount() default 0;

	/**
	 * The maximal allowed count this option can be specified. Use -1 to specify
	 * infinity.
	 */
	int maxCount() default 1;
}
