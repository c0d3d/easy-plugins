package com.nlocketz.example1;

import com.nlocketz.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service(value = "Feature",
        serviceInterface = "com.nlocketz.example1.ActualFeatureServiceInterface",
        outputPackage = "out.pkg")
public @interface Feature {
    String value();
}
