package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;

import java.util.ServiceLoader;

import static com.nlocketz.internal.Constants.*;
import static com.nlocketz.internal.Util.privateField;
import static com.nlocketz.internal.Util.privateStaticField;

/**
 * Builds the "registry" for a service.
 * The registry is the class that you can query for different service providers, and construct them.
 */
class PluginRegistryFileGenerator extends AbstractPluginFileGenerator {

    protected PluginRegistryFileGenerator(ProcessingEnvironment procEnv, RoundEnvironment roundEnv) {
        super(procEnv, roundEnv);
    }

    @Override
    public void generate(UserMarkerAnnotation marker, ProcessorOutputCollection into) {
        String registryClassName = marker.getRegistryServiceName();
        ClassName spiName = ClassName.get(marker.getOutputPackage(elements), marker.getServiceInterfaceProviderName());
        ClassName registryName = ClassName.get(marker.getOutputPackage(elements), registryClassName);
        TypeName threadName = TypeName.get(Thread.class);
        ParameterizedTypeName genericServiceLoaderName = ParameterizedTypeName.get(SERVICE_LOADER_CLASS_NAME, spiName);
        ParameterizedTypeName genericMapName = ParameterizedTypeName.get(MAP_CLASS_NAME, STRING_TYPE_NAME, spiName);
        ParameterizedTypeName genericHashmapName = ParameterizedTypeName.get(HASHMAP_CLASS_NAME, STRING_TYPE_NAME, spiName);
        TypeSpec.Builder classSpecBuilder = TypeSpec.classBuilder(registryClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(privateStaticField(registryName, INSTANCE_FIELD_NAME).build())
                .addField(privateField(genericMapName, PLUGIN_MAP).build())
                .addMethod(
                        MethodSpec.methodBuilder("getInstance")
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .returns(registryName)
                                .beginControlFlow("if ($L == null)", INSTANCE_FIELD_NAME)
                                .addStatement("$L = new $T()", INSTANCE_FIELD_NAME, registryName)
                                .endControlFlow()
                                .addStatement("return $L", INSTANCE_FIELD_NAME)
                                .build())
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addStatement("$T.currentThread().setContextClassLoader($T.class.getClassLoader())", threadName, spiName)
                                .addStatement("$L = new $T()", PLUGIN_MAP, genericHashmapName)
                                .addStatement("$T $L = $T.load($T.class)",
                                        genericServiceLoaderName,
                                        SERVICE_LOADER_FIELD_NAME,
                                        SERVICE_LOADER_CLASS_NAME,
                                        spiName)
                                .beginControlFlow("for ($T s : $L)", spiName, SERVICE_LOADER_FIELD_NAME)
                                .addStatement("$L.put(s.$L(), s)", PLUGIN_MAP, GET_NAME_METHOD_NAME)
                                .endControlFlow()
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+marker.getServiceInterfaceName()+"ByName")
                                .returns(marker.getServiceInterfaceTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, PROVIDER_NAME_ARG_NAME)
                                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                                        PLUGIN_MAP, PROVIDER_NAME_ARG_NAME)
                                .addStatement("return getInstance().$L.get($L).$L()",
                                        PLUGIN_MAP, PROVIDER_NAME_ARG_NAME, CREATE_NEW_METHOD_NAME)
                                .endControlFlow()
                                .addStatement("return null")
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+marker.getServiceInterfaceName()+"ByNameWithConfig")
                                .returns(marker.getServiceInterfaceTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, PROVIDER_NAME_ARG_NAME)
                                .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME)
                                .beginControlFlow("if (getInstance().$L.containsKey($L))",
                                        PLUGIN_MAP, PROVIDER_NAME_ARG_NAME)
                                .addStatement("return getInstance().$L.get($L).createWithConfig($L)",
                                        PLUGIN_MAP, PROVIDER_NAME_ARG_NAME, CONFIG_NAME_ARG_NAME)
                                .endControlFlow()
                                .addStatement("return null")
                                .build());

        ServiceLoader<EasyPluginPlugin> pluginServiceLoader = ServiceLoader.load(EasyPluginPlugin.class);
        for (EasyPluginPlugin plugin : pluginServiceLoader) {
            for (MethodSpec methodSpec : plugin.registryMethods(marker)) {
                classSpecBuilder = classSpecBuilder.addMethod(methodSpec);
            }
        }

        TypeSpec classSpec = classSpecBuilder.build();

        into.putType(marker.getOutputPackage(elements), classSpec);
    }
}
