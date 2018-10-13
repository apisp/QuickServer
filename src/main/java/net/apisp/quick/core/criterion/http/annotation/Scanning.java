/**
 * Copyright (c) 2018, All Rights Reserved. 
 */
package net.apisp.quick.core.criterion.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scanning {
    Class<?>[] value() default {};
}
