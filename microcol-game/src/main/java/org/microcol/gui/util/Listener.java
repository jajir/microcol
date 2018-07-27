package org.microcol.gui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Allows to mark classes that will listen events.
 */
@BindingAnnotation
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {

}
