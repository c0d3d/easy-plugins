package com.nlocketz.internal;

import com.nlocketz.Service;
import com.squareup.javapoet.MethodSpec;

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
     * Produces a list of static methods which are to be added to the generated registry.
     * @param annotation An annotated service class (i.e. one annotated with {@link Service})
     * @return A list of static registry methods
     */
    List<MethodSpec> registryMethods(UserMarkerAnnotation annotation);

    /**
     * Produces a list of implementations for the methods returned by {@link #pluginProviderInterfaceMethods(UserMarkerAnnotation)}
     * for the given annotated class.
     * @param markedPluginClass A class which has been annotated with an annotation that is
     *                          annotated with {@link Service} (try saying that 10 times fast)
     * @return The implementations of the plugin provider methods.
     */
    List<MethodSpec> pluginProviderMethods(MarkedPluginClass markedPluginClass);

    /**
     * Produces a list of interface method declarations to be placed in the auto-generated
     * interface for the given service class.
     * @param annotation An annotated service class (i.e. one annotated with {@link Service})
     * @return The interface method declarations
     */
    List<MethodSpec> pluginProviderInterfaceMethods(UserMarkerAnnotation annotation);

}
