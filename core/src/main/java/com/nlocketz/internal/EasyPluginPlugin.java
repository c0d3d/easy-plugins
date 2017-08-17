package com.nlocketz.internal;

import com.squareup.javapoet.MethodSpec;

import java.util.List;

public interface EasyPluginPlugin {

    List<MethodSpec> registryMethods(UserMarkerAnnotation annotation);

    List<MethodSpec> pluginProviderMethods(MarkedPluginClass markedPluginClass);

    List<MethodSpec> pluginProviderInterfaceMethods(UserMarkerAnnotation annotation);

}
