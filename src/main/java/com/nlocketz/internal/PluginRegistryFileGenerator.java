package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import java.util.ServiceLoader;

import static com.nlocketz.internal.Constants.MAP_STRING_STRING_NAME;
import static com.nlocketz.internal.Constants.SERVICE_LOADER_CLASS_NAME;

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
        ClassName spiName = ClassName.get(marker.getOutputPackage(), marker.getServiceInterfaceProviderName());
        ClassName registryName = ClassName.get(marker.getOutputPackage(), registryClassName);
        ParameterizedTypeName genericServiceLoaderName = ParameterizedTypeName.get(SERVICE_LOADER_CLASS_NAME, spiName);

        TypeSpec classSpec = TypeSpec.classBuilder(registryClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(
                        Util.privateStaticField(registryName, Constants.INSTANCE_FIELD_NAME).build())
                .addField(
                        Util.privateField(genericServiceLoaderName, Constants.SERVICE_LOADER_FIELD_NAME).build())
                .addMethod(
                        MethodSpec.methodBuilder("getInstance")
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .returns(registryName)
                                .beginControlFlow("if ($L == null)", Constants.INSTANCE_FIELD_NAME)
                                .addStatement("$L = new $T()", Constants.INSTANCE_FIELD_NAME, registryName)
                                .endControlFlow()
                                .addStatement("return $L", Constants.INSTANCE_FIELD_NAME)
                                .build())
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addStatement("$L = $T.load($T.class)",
                                        Constants.SERVICE_LOADER_FIELD_NAME,
                                        ServiceLoader.class,
                                        spiName)
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+marker.getServiceInterfaceName()+"ByName")
                                .returns(marker.getServiceInterfaceTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, Constants.PROVIDER_NAME_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, Constants.SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", Constants.PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.create()")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+marker.getServiceInterfaceName()+"ByNameWithConfig")
                                .returns(marker.getServiceInterfaceTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, Constants.PROVIDER_NAME_ARG_NAME)
                                .addParameter(MAP_STRING_STRING_NAME, Constants.CONFIG_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, Constants.SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", Constants.PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.createWithConfig($L)", Constants.CONFIG_NAME_ARG_NAME)
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .build();

        into.putType(marker.getOutputPackage(), classSpec);
    }
}
