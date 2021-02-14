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
		EMBED_OPTIONS,
		FIND_COMMAND
	}

	Mode value() default Mode.EMBED_OPTIONS;
}
