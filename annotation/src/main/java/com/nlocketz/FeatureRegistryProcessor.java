package com.nlocketz;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;


@SupportedAnnotationTypes("com.nlocketz.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class FeatureRegistryProcessor extends AbstractProcessor {

    private static final String INSTANCE_FIELD_NAME = "instance";
    private static final String SERVICE_LOADER_FIELD_NAME = "loader";
    private static final String PROVIDER_NAME_ARG_NAME = "name";
    private static final String CONFIG_NAME_ARG_NAME = "config";

    private static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);
    private static final TypeName STRING_TYPE_NAME = TypeName.get(String.class);
    private static final ClassName MAP_CLASS_NAME = ClassName.get(Map.class);
    private static final ParameterizedTypeName MAP_STRING_STRING_NAME =
            ParameterizedTypeName.get(MAP_CLASS_NAME, STRING_CLASS_NAME, STRING_CLASS_NAME);
    private static final String GET_NAME_METHOD_NAME = "getProviderName";
    private static final String CREATE_NEW_METHOD_NAME = "create";
    private static final String CREATE_NEW_WITH_CONFIG_METHOD_NAME = "createWithConfig";
    private static final String CONFIG_ARG_NAME = "config";


    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        try {
            for (TypeElement ele : annotations) {
                for (Element newService : roundEnvironment.getElementsAnnotatedWith(ele)) {
                    processService(newService, roundEnvironment);
                }
            }
        } catch (EZServiceException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void processService(Element annotationElement, RoundEnvironment roundEnv) {

        ServiceAnnotation newService = new ServiceAnnotation(annotationElement, processingEnv);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "Making services for " +newService.getServiceInterfaceName());

        Set<? extends MarkedServiceClass> markedClasses = newService.getMarkedClasses(roundEnv, processingEnv);
        TypeSpec serviceInterface = buildServiceInterfaceFor(newService);
        writeSpec(serviceInterface, newService.getOutputPackage());

        for (MarkedServiceClass msc : markedClasses) {
            TypeSpec singleClass =
                    buildSingleClass(msc, newService.getServiceInterfaceName(), newService.getOutputPackage());
            writeSpec(singleClass, newService.getOutputPackage());
        }

        TypeSpec registryClass = buildRegistryClass(newService.getServiceRegistryName(), newService);
        writeSpec(registryClass, newService.getOutputPackage());
    }


    private TypeSpec buildRegistryClass(String service, ServiceAnnotation annotation) {
        ClassName spiName = ClassName.get(annotation.getOutputPackage(), annotation.getServiceInterfaceName());
        ClassName registryName = ClassName.get(annotation.getOutputPackage(), service);
        ClassName serviceLoaderName = ClassName.get(ServiceLoader.class);
        ClassName mapClassName = ClassName.get(Map.class);
        ClassName stringClassName = ClassName.get(String.class);
        ParameterizedTypeName genericServiceLoaderName = ParameterizedTypeName.get(serviceLoaderName, spiName);
        ParameterizedTypeName genericMap = ParameterizedTypeName.get(mapClassName, stringClassName, stringClassName);

        return TypeSpec.classBuilder(service)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(
                        FieldSpec.builder(registryName, INSTANCE_FIELD_NAME)
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .build())
                .addField(
                        FieldSpec.builder(genericServiceLoaderName, SERVICE_LOADER_FIELD_NAME)
                                .addModifiers(Modifier.PRIVATE)
                                .build())
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
                                .addStatement("$L = $T.load($T.class)",
                                        SERVICE_LOADER_FIELD_NAME,
                                        ServiceLoader.class,
                                        spiName)
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+annotation.getServiceName()+"ByName")
                                .returns(annotation.getProviderReturnTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, PROVIDER_NAME_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.create()")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .addMethod(
                        MethodSpec.methodBuilder("get"+annotation.getServiceName()+"ByNameWithConfig")
                                .returns(annotation.getProviderReturnTypeName())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(String.class, PROVIDER_NAME_ARG_NAME)
                                .addParameter(genericMap, CONFIG_ARG_NAME)
                                .beginControlFlow("for ($T s : getInstance().$L)", spiName, SERVICE_LOADER_FIELD_NAME)
                                .beginControlFlow("if (s.getProviderName().equals($L))", PROVIDER_NAME_ARG_NAME)
                                .addStatement("return s.createWithConfig($L)", CONFIG_NAME_ARG_NAME)
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return null").build())
                .build();
    }

    private void writeSpec(TypeSpec spec, String outputPkg) {
        JavaFile file = JavaFile.builder(outputPkg, spec).build();
        try {
            file.writeTo(processingEnv.getFiler());
        } catch (FilerException e) {
            throw new EZServiceException("Couldn't create file: "+e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private TypeSpec buildServiceInterfaceFor(ServiceAnnotation newService) {
        return TypeSpec.interfaceBuilder(newService.getServiceInterfaceName())
                .addMethod(
                        MethodSpec.methodBuilder(GET_NAME_METHOD_NAME)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(STRING_TYPE_NAME)
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder(CREATE_NEW_METHOD_NAME)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .returns(newService.getProviderReturnTypeName())
                                .build())
                .addMethod(
                        MethodSpec.methodBuilder(CREATE_NEW_WITH_CONFIG_METHOD_NAME)
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME)
                                .returns(newService.getProviderReturnTypeName())
                                .build())
                .build();
    }

    private TypeSpec buildSingleClass(MarkedServiceClass msc, String interfaceName, String outputPkg) {
        String className = msc.getNewServiceClassName();

        MethodSpec getName = MethodSpec.methodBuilder(GET_NAME_METHOD_NAME)
                .returns(STRING_TYPE_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $L", msc.getServiceName())
                .build();

        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder(CREATE_NEW_METHOD_NAME)
                .returns(msc.getTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        msc.addDefaultConstructorCall(createBuilder);

        MethodSpec.Builder createWithConfigBuilder = MethodSpec.methodBuilder(CREATE_NEW_WITH_CONFIG_METHOD_NAME)
                .returns(msc.getTypeName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME);
        msc.addMapConstructorCall(createWithConfigBuilder, CONFIG_ARG_NAME);

        return TypeSpec.classBuilder(className)
                .addAnnotation(
                        AnnotationSpec.builder(AutoService.class)
                                .addMember("value", "$L.class", interfaceName)
                                .build())
                .addSuperinterface(ClassName.get(outputPkg, interfaceName))
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(getName)
                .addMethod(createBuilder.build())
                .addMethod(createWithConfigBuilder.build())
                .build();
    }
}
