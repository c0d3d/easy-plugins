package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Builds the "registry" for a service.
 * The registry is the class that you can query for different service providers, and construct them.
 */
class ServiceRegistryFileBuilder implements ServiceFileBuilder {
    @Override
    public List<JavaFile> buildFiles(ServiceAnnotation annotation, RoundEnvironment roundEnv, ProcessingEnvironment procEnv) {
        String registryClassName = annotation.getServiceRegistryName();
        ClassName spiName = ClassName.get(annotation.getOutputPackage(), annotation.getServiceInterfaceName());
        ClassName registryName = ClassName.get(annotation.getOutputPackage(), registryClassName);
        ClassName serviceLoaderName = ClassName.get(ServiceLoader.class);
        ClassName mapClassName = ClassName.get(Map.class);
        ClassName stringClassName = ClassName.get(String.class);
        ParameterizedTypeName genericServiceLoaderName = ParameterizedTypeName.get(serviceLoaderName, spiName);
        ParameterizedTypeName genericMap = ParameterizedTypeName.get(mapClassName, stringClassName, stringClassName);

        TypeSpec classSpec = TypeSpec.classBuilder(registryClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(
                        PoetUtil.privateStaticField(registryName, GeneratedNameConstants.INSTANCE_FIELD_NAME).build())
                .addField(
                        PoetUtil.privateField(genericServiceLoaderName, GeneratedNameConstants.SERVICE_LOADER_FIELD_NAME).build())
                .addMethod(
                        MethodSpec.methodBuilder("getInstance")
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .returns(registryName)
                                .beginControlFlow("if ($L == null)", GeneratedNameConstants.INSTANCE_FIELD_NAME)
                                .addStatement("$L = new $T()", GeneratedNameConstants.INSTANCE_FIELD_NAME, registryName)
                                .endControlFlow()
                                .addStatement("return $L", GeneratedNameConstants.INSTANCE_FIELD_NAME)
                                .build())
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addStatement("$L = $T.load($T.class)",
                                        GeneratedNameConstants.SERVICE_LOADER_FIELD_NAME,
                                        ServiceLoader.class,
                                        spiName)
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+annotation.getServiceName()+"ByName")
                                .returns(annotation.getProviderReturnTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, GeneratedNameConstants.PROVIDER_NAME_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, GeneratedNameConstants.SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", GeneratedNameConstants.PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.create()")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+annotation.getServiceName()+"ByNameWithConfig")
                                .returns(annotation.getProviderReturnTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, GeneratedNameConstants.PROVIDER_NAME_ARG_NAME)
                                .addParameter(genericMap, GeneratedNameConstants.CONFIG_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, GeneratedNameConstants.SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", GeneratedNameConstants.PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.createWithConfig($L)", GeneratedNameConstants.CONFIG_NAME_ARG_NAME)
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .build();

        return Collections.singletonList(
                JavaFile.builder(annotation.getOutputPackage(), classSpec).build());
    }
}
