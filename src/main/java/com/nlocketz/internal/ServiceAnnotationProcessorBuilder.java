package com.nlocketz.internal;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.nlocketz.internal.GeneratedNameConstants.*;

public class ServiceAnnotationProcessorBuilder implements ServiceFileBuilder {
    @Override
    public List<JavaFile> buildFiles(ServiceAnnotation annotation,
                                     RoundEnvironment roundEnv,
                                     ProcessingEnvironment procEnv) {

        MethodSpec writeFile =
                PoetUtil.privateMethod(WRITE_FILE_METHOD_NAME, TypeName.VOID)
                        .addParameter(JAVA_FILE_CLASS_NAME, "file")
                        .beginControlFlow("try")
                        .addStatement("$L.writeTo($L.getFiler())", "file", PROCESSING_ENV_NAME)
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", FILER_EXCEPTION_CLASS_NAME)
                        .addStatement(
                                "throw new $T($S + $L.getMessage())",
                                EZ_SERVICE_EXCEPTION_CLASS_NAME, "Couldn't create file: ", "e")
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", IOException.class)
                        .addStatement("throw new $T($L)", RuntimeException.class, "e")
                        .endControlFlow()
                        .build();

        MethodSpec processAnnotatedElement =
                PoetUtil.privateMethod(PROCESS_ANNOTATED_ELEMENT_METHOD_NAME, TypeName.VOID)
                        .addParameter(ELEMENT_CLASS_NAME, PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME)
                        .addStatement("$T allFiles = $T.buildServiceFiles($L, $L, $L)",
                                LIST_JAVA_FILE_NAME,
                                COMPLETE_SERVICE_BUILDER_CLASS_NAME,
                                PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME,
                                ROUND_ENV_NAME,
                                PROCESSING_ENV_NAME)
                        .beginControlFlow("for ($T file : $L)", JAVA_FILE_CLASS_NAME, "allFiles")
                        .addStatement("$N(file)", writeFile)
                        .endControlFlow()
                        .build();


        MethodSpec process =
                PoetUtil.publicFinalMethod("process", TypeName.BOOLEAN)
                        .addParameter(SET_WILD_EXTENDS_ELE, PROCESS_METHOD_ARG_SET_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME)
                        .beginControlFlow("try")
                        .beginControlFlow("for ($T annotation : $L)", TYPE_ELEMENT_CLASS_NAME, PROCESS_METHOD_ARG_SET_NAME)
                        .addComment("We have to create source files for any annotated elements.")
                        .beginControlFlow(
                                "for ($T annotatedEle : $L.getElementsAnnotatedWith($L))",
                                ELEMENT_CLASS_NAME,
                                ROUND_ENV_NAME,
                                "annotation")
                        .addStatement("$N($L, $L)", processAnnotatedElement, "annotatedEle", ROUND_ENV_NAME)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", EZ_SERVICE_EXCEPTION_CLASS_NAME)
                        .addComment("See comment on $T", EZ_SERVICE_EXCEPTION_CLASS_NAME)
                        .addStatement(
                                "$L.getMessager().printMessage($T.$L, $L.getMessage())", PROCESSING_ENV_NAME, Diagnostic.Kind.class, "ERROR", "e")
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", Exception.class)
                        .addComment("Everthing else is a problem ...")
                        .addStatement("$L.printStackTrace()", "e")
                        .addStatement("throw new $T($L)", RuntimeException.class, "e")
                        .endControlFlow()
                        .addComment("Done ...")
                        .addStatement("return false")
                        .build();

        TypeSpec procSpec =
                TypeSpec.classBuilder(annotation.getServiceName()+"Processor")
                        .superclass(ABSTRACT_PROCESSOR_CLASS_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(
                                AnnotationSpec.builder(SUPPORTED_ANNOTATION_TYPES_CLASS_NAME)
                                        .addMember("value", "$S", annotation.getMirrorForMarkerAnnotation().toString())
                                        .build())
                        .addAnnotation(
                                AnnotationSpec.builder(SUPPORTED_SOURCE_VERSION_CLASS_NAME)
                                        .addMember("value", "$T.RELEASE_8", SOURCE_VERSION_CLASS_NAME)
                                        .build())
                        .addAnnotation(
                                AnnotationSpec.builder(AUTO_SERVICE_CLASS_NAME)
                                        .addMember("value", "$T.class", PROCESSOR_CLASS_NAME)
                                        .build())
                        .addMethod(process)
                        .addMethod(processAnnotatedElement)
                        .addMethod(writeFile)
                        .build();

        return ImmutableList.of(JavaFile.builder(annotation.getOutputPackage(), procSpec).build());

    }
}
