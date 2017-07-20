package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.nlocketz.internal.GeneratedNameConstants.*;

public class ServiceAnnotationProcessorBuilder extends AbstractServiceFileBuilder {

    ServiceAnnotationProcessorBuilder(CompleteServiceBuilder overallBuilder) {
        super(overallBuilder);
    }

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
                        .addComment("Already exists ...")
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", IOException.class)
                        .addStatement("throw new $T($L)", RuntimeException.class, "e")
                        .endControlFlow()
                        .build();

        MethodSpec.Builder processBuilder =
                PoetUtil.publicFinalMethod("process", TypeName.BOOLEAN)
                        .addParameter(SET_WILD_EXTENDS_ELE, PROCESS_METHOD_ARG_SET_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME);

        String markerAnnotationName =
                MarkerAnnotation.addMarkerAnnotationInstance(processBuilder, annotation, procEnv.getTypeUtils());

        MethodSpec processAnnotatedElement =
                PoetUtil.privateMethod(PROCESS_ANNOTATED_ELEMENT_METHOD_NAME, TypeName.VOID)
                        .addParameter(ELEMENT_CLASS_NAME, PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME)
                        .addParameter(MARKER_ANNOTATION_CLASS_NAME, "marker")
                        .addStatement("$T allFiles = $T.buildSpecializedServiceFiles($L, $L, $L, $L)",
                                LIST_JAVA_FILE_NAME,
                                COMPLETE_SERVICE_BUILDER_CLASS_NAME,
                                PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME,
                                markerAnnotationName,
                                ROUND_ENV_NAME,
                                PROCESSING_ENV_NAME)
                        .beginControlFlow("for ($T file : $L)", JAVA_FILE_CLASS_NAME, "allFiles")
                        .addStatement("$N(file)", writeFile)
                        .endControlFlow()
                        .build();



        MethodSpec process =
                processBuilder.beginControlFlow("try")
                        .beginControlFlow("for ($T annotation : $L)", TYPE_ELEMENT_CLASS_NAME, PROCESS_METHOD_ARG_SET_NAME)
                        .addComment("We have to create source files for any annotated elements.")
                        .addStatement("$N($L, $L, $L)", processAnnotatedElement, "annotation", ROUND_ENV_NAME, markerAnnotationName)
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
                TypeSpec.classBuilder(annotation.getServiceName() + "Processor")
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
                        .addMethod(process)
                        .addMethod(processAnnotatedElement)
                        .addMethod(writeFile)
                        .build();
        String currentPackage = this.getClass().getPackage().getName();
        String processorQName = currentPackage + "." + annotation.getServiceName() + "Processor";

        // Write the META-INF service file for the new processor.
        overallBuilder.addToSpiOutput(Processor.class.getName(), Collections.singleton(processorQName));

        return Collections.singletonList(JavaFile.builder(currentPackage, procSpec).build());

    }
}
