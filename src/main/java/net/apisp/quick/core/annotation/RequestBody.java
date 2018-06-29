/**
 * Copyright (c) 2018-present, APISP.NET. 
 */
package net.apisp.quick.core.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RequestBody {

}
