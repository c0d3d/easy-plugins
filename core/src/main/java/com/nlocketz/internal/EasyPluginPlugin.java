package com.nlocketz.internal;

import com.nlocketz.Service;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

/**
 * Experimental Service API for extending the behavior of easy-plugins.
 * Plugins can be loaded by placing themselves under {@code META-INF/services}
 * such that the Java SPI can load them (e.g. by marking the class with
 * {@code @AutoService(EasyPluginPlugin.class)}, using Google's auto-service package).
 * Note that the irony of a project titled "easy-plugins" not having a built-in
 * annotation which easily allows plugin development is not lost on the developers.
 */
public interface EasyPluginPlugin {

    /**
     * Modifies the given {@code easy-plugins} registry.
     * @param registry The registry to modify
     * @param annotation An annotated service class (i.e. one annotated with {@link Service})
     */
    void updateRegistry(TypeSpec.Builder registry, UserMarkerAnnotation annotation);

    /**
     * Modifies the given implementation of this {@link Service}'s provider associated
     * with the given annotated class.
     * @param provider The SPI implementation to modify
     * @param markedPluginClass A class which has been annotated with an annotation that is
     *                          annotated with {@link Service} (try saying that 10 times fast)
     */
    void updatePluginProvider(TypeSpec.Builder provider, MarkedPluginClass markedPluginClass);

    /**
     * Modifies the auto-generated interface for the given service class.
     * @param serviceInterface The SPI interface to modify
     * @param annotation An annotated service class (i.e. one annotated with {@link Service})
     */
    void updatePluginProviderInterface(TypeSpec.Builder serviceInterface, UserMarkerAnnotation annotation);

}
