package de.tototec.cmdoption;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
public @interface CmdCommand {

	/**
	 * The name of this command.
	 */
	String[] names();

	/**
	 * the description of this command.
	 */
	String description() default "";

	/**
	 * If <code>true</code>, this command is not shown in the usage output.
	 */
	boolean hidden() default false;

}
