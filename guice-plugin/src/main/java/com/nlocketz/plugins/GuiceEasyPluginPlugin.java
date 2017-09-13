package com.nlocketz.plugins;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.nlocketz.internal.EasyPluginPlugin;
import com.nlocketz.internal.MarkedPluginClass;
import com.nlocketz.internal.UserMarkerAnnotation;
import com.nlocketz.internal.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;

/**
 * A plugin for easy-plugins which adds the ability to perform
 * Guice injections on plugins. <strong>NOTE:</strong> currently,
 * constructor injections are not supported. The plugin adds the
 * following methods to generated registries:
 * <ul>
 *     <li><pre>#getInjectedXByName({@link String}, {@link Injector})</pre></li>
 *     <li><pre>#getInjectedXByNameWithConfig({@link String}, {@link Map}&lt;{@link String}, {@link String}&gt;, {@link Injector})</pre></li>
 * </ul>
 * Currently, these simply perform instance injections using the
 * given injectors. In the future, however, we would like to be able
 * to support constructor injections.
 */
@AutoService(EasyPluginPlugin.class)
public class GuiceEasyPluginPlugin implements EasyPluginPlugin {
    private static final ClassName STRING_NAME = ClassName.get(String.class);
    private static final ClassName MAP_NAME = ClassName.get(Map.class);
    private static final ParameterizedTypeName MAP_STRING_STRING_NAME = ParameterizedTypeName.get(MAP_NAME, STRING_NAME, STRING_NAME);
    private static final TypeName INJECTOR_NAME = ClassName.get(Injector.class);

    @Override
    public void updateRegistry(TypeSpec.Builder registry, UserMarkerAnnotation annotation) {

        MethodSpec injectedWithoutConfig = MethodSpec.methodBuilder("getInjected"+annotation.getServiceInterfaceName()+"ByName")
                .returns(annotation.getServiceInterfaceTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .addParameter(INJECTOR_NAME, "injector")
                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                        "pluginMap", "name")
                .addStatement("return getInstance().$L.get($L).createInjected($L)",
                        "pluginMap", "name", "injector")
                .endControlFlow()
                .addStatement("return null")
                .build();

        MethodSpec injectedWithConfig = MethodSpec.methodBuilder("getInjected"+annotation.getServiceInterfaceName()+"ByNameWithConfig")
                .returns(annotation.getServiceInterfaceTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .addParameter(MAP_STRING_STRING_NAME, "config")
                .addParameter(INJECTOR_NAME, "injector")
                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                        "pluginMap", "name")
                .addStatement("return getInstance().$L.get($L).createInjectedWithConfig($L, $L)",
                        "pluginMap", "name", "config", "injector")
                .endControlFlow()
                .addStatement("return null")
                .build();

        registry.addMethod(injectedWithoutConfig);
        registry.addMethod(injectedWithConfig);
    }

    @Override
    public void updatePluginProvider(TypeSpec.Builder provider, MarkedPluginClass markedPluginClass) {

        MethodSpec injectedWithoutConfig = Util.publicFinalMethod("createInjected", markedPluginClass.getTypeName())
                .addParameter(INJECTOR_NAME, "injector")
                .addStatement("$T ret = this.create()", markedPluginClass.getTypeName())
                .addStatement("$L.injectMembers($L)", "injector", "ret")
                .addStatement("return ret")
                .build();
        MethodSpec injectedWithConfig = Util.publicFinalMethod("createInjectedWithConfig", markedPluginClass.getTypeName())
                .addParameter(MAP_STRING_STRING_NAME, "config")
                .addParameter(INJECTOR_NAME, "injector")
                .addStatement("$T ret = this.createWithConfig($L)", markedPluginClass.getTypeName(), "config")
                .addStatement("$L.injectMembers($L)", "injector", "ret")
                .addStatement("return ret")
                .build();

        provider.addMethod(injectedWithoutConfig);
        provider.addMethod(injectedWithConfig);
    }

    @Override
    public void updatePluginProviderInterface(TypeSpec.Builder serviceInterface, UserMarkerAnnotation annotation) {
        TypeName returnType = annotation.getServiceInterfaceTypeName();
        MethodSpec createInjected = Util.publicAbstractMethod("createInjected", returnType)
                .addParameter(INJECTOR_NAME, "injector")
                .build();
        MethodSpec createInjectedWithConfig = Util.publicAbstractMethod("createInjectedWithConfig", returnType)
                .addParameter(MAP_STRING_STRING_NAME, "config")
                .addParameter(INJECTOR_NAME, "injector")
                .build();

        serviceInterface.addMethod(createInjected);
        serviceInterface.addMethod(createInjectedWithConfig);
    }
}
