package com.nlocketz.plugins;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.nlocketz.internal.EasyPluginPlugin;
import com.nlocketz.internal.MarkedPluginClass;
import com.nlocketz.internal.UserMarkerAnnotation;
import com.nlocketz.internal.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;

@AutoService(EasyPluginPlugin.class)
public class GuiceEasyPluginPlugin implements EasyPluginPlugin {
    private static final ClassName STRING_NAME = ClassName.get(String.class);
    private static final ClassName MAP_NAME = ClassName.get(Map.class);
    private static final ParameterizedTypeName MAP_STRING_STRING_NAME = ParameterizedTypeName.get(MAP_NAME, STRING_NAME, STRING_NAME);
    private static final TypeName MODULE_NAME = ClassName.get(Module.class);

    @Override
    public List<MethodSpec> registryMethods(UserMarkerAnnotation annotation) {

        MethodSpec injectedWithoutConfig = MethodSpec.methodBuilder("getInjected"+annotation.getServiceInterfaceName()+"ByName")
                .returns(annotation.getServiceInterfaceTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .addParameter(MODULE_NAME, "module")
                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                        "pluginMap", "name")
                .addStatement("return getInstance().$L.get($L).createInjected($L)",
                        "pluginMap", "name", "module")
                .endControlFlow()
                .addStatement("return null")
                .build();

        MethodSpec injectedWithConfig = MethodSpec.methodBuilder("getInjected"+annotation.getServiceInterfaceName()+"ByNameWithConfig")
                .returns(annotation.getServiceInterfaceTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .addParameter(MAP_STRING_STRING_NAME, "config")
                .addParameter(MODULE_NAME, "module")
                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                        "pluginMap", "name")
                .addStatement("return getInstance().$L.get($L).createInjectedWithConfig($L, $L)",
                        "pluginMap", "name", "config", "module")
                .endControlFlow()
                .addStatement("return null")
                .build();

        return Lists.newArrayList(injectedWithoutConfig, injectedWithConfig);
    }

    @Override
    public List<MethodSpec> pluginProviderMethods(MarkedPluginClass markedPluginClass) {
        return null;
    }

    @Override
    public List<MethodSpec> pluginProviderInterfaceMethods(UserMarkerAnnotation annotation) {
        TypeName returnType = annotation.getServiceInterfaceTypeName();
        MethodSpec createInjected = Util.publicAbstractMethod("createInjected", returnType).build();
        MethodSpec createInjectedWithConfig = Util.publicAbstractMethod("createInjectedWithConfig", returnType).build();
        return Lists.newArrayList(createInjected, createInjectedWithConfig);
    }
}
