package de.tototec.cmdoption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.tototec.cmdoption.handler.CmdOptionHandler;

/**
 * An Command line option which optionally supports parameters. It can be used
 * to annotate fields and methods as options. At most on field or method can be
 * annotated with an zero names attribute, which means that field or method
 * represents the main parameter .
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface CmdOption {

	/**
	 * The names of this option. If not specified, the annotated field or method
	 * represents the main parameter. At most on main parameter is allowed.
	 */
	String[] names() default {};

	/**
	 * The description of the option.
	 */
	String description() default "";

	/**
	 * The arguments (their names) supported by this option. The count of
	 * arguments is used, to determite the {@link CmdOptionHandler} to use. The
	 * names are used in messages and the usage display.
	 */
	String[] args() default {};

	/**
	 * An {@link CmdOptionHandler} to apply the parsed option to the annotated
	 * field or method. If this is not given, all handler registered for
	 * auto-detect will by tried in order.
	 * 
	 * @see CmdlineParser#registerHandler(CmdOptionHandler)
	 */
	Class<? extends CmdOptionHandler> handler() default CmdOptionHandler.class;

	/**
	 * The minimal allowed count this option can be specified. Optional options
	 * have 0 here, which is the default.
	 */
	int minCount() default 0;

	/**
	 * The maximal allowed count this option can be specified. Use -1 to specify
	 * infinity.
	 */
	int maxCount() default 1;

	/**
	 * Special marker, that this option is a help request. Typically, such an
	 * option is used to display a usage information to the user and exit. If
	 * such an option is parsed, validation will be disabled to allow help
	 * request even when the command line is incorrect.
	 */
	boolean isHelp() default false;

	/**
	 * If <code>true</code>, do not show this option in the usage.
	 */
	boolean hidden() default false;

	/**
	 * If this option is only valid in conjunction with other options, you
	 * should declare those other options here.
	 */
	String[] requires() default {};

	/**
	 * If this option can not be used in conjunction with an specific other
	 * option, you should declare those conflicting options here.
	 */
	String[] conflictsWith() default {};

}
