package com.collectz.qlombok;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author Bruce Shi
 * @since 8/1/2020.
 */

@Target(TYPE)
@Retention(SOURCE)
public @interface PresenceCheck {

    String value() default "";
}
