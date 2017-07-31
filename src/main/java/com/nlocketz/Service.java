package com.nlocketz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value();
    Class<?> serviceInterface();

    /**
     * This is the name of the field in the annotation annotated with this annotation to look for the name
     * of the service when you mark a class.
     * @return the name of the field to look for service names in
     */
    String serviceNameKey() default "value";

    /**
     * The output package of the generated classes.
     * Default is {@code ""} which means output in the same package as the service.
     * @return The output package for generated classes.
     */
    String outputPackage() default "";

}
