package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;

import static com.nlocketz.internal.GeneratedNameConstants.*;

public class PluginAnnotationProcessorGenerator extends AbstractPluginFileGenerator {

    protected PluginAnnotationProcessorGenerator(ProcessingEnvironment procEnv,
                                                 RoundEnvironment roundEnv) {
        super(procEnv, roundEnv);
    }

    @Override
    public void generate(UserMarkerAnnotation marker, ProcessorOutputCollection into) {

        MethodSpec.Builder processBuilder =
                Util.publicFinalMethod("process", TypeName.BOOLEAN)
                        .addParameter(SET_WILD_EXTENDS_ELE, PROCESS_METHOD_ARG_SET_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME);

        String markerAnnotationName =
                MarkerAnnotation.addMarkerAnnotationInstance(processBuilder, marker, types);

        MethodSpec processAnnotatedElement =
                Util.privateMethod(PROCESS_ANNOTATED_ELEMENT_METHOD_NAME, TypeName.VOID)
                        .addParameter(ELEMENT_CLASS_NAME, PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME)
                        .addParameter(ROUND_ENV_CLASS_NAME, ROUND_ENV_NAME)
                        .addParameter(MARKER_ANNOTATION_CLASS_NAME, "marker")
                        .addParameter(PROC_OUT_COLL_CLASS_NAME, "out")
                        .addStatement("$T.buildSpecializedServiceFiles($L, $L, $L, $L, $L)",
                                COMPLETE_SERVICE_BUILDER_CLASS_NAME,
                                PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME,
                                markerAnnotationName,
                                ROUND_ENV_NAME,
                                PROCESSING_ENV_NAME,
                                "out")
                        .build();



        MethodSpec process =
                processBuilder
                        .addStatement("$T $L = $T.empty()", PROC_OUT_COLL_CLASS_NAME, "out", PROC_OUT_COLL_CLASS_NAME)
                        .beginControlFlow("try")
                        .beginControlFlow("for ($T annotation : $L)", TYPE_ELEMENT_CLASS_NAME, PROCESS_METHOD_ARG_SET_NAME)
                        .addComment("We have to create source files for any annotated elements.")
                        .addStatement("$N($L, $L, $L, $L)", processAnnotatedElement, "annotation", ROUND_ENV_NAME, markerAnnotationName, "out")
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
                        .addStatement("$L.writeContents($L.getFiler())", "out", PROCESSING_ENV_NAME)
                        .addComment("Done ...")
                        .addStatement("return false")
                        .build();

        TypeSpec procSpec =
                TypeSpec.classBuilder(marker.getProcessorServiceName() + "Processor")
                        .superclass(ABSTRACT_PROCESSOR_CLASS_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(
                                AnnotationSpec.builder(SUPPORTED_ANNOTATION_TYPES_CLASS_NAME)
                                        .addMember("value", "$S", marker.getMarkerAnnotationType())
                                        .build())
                        .addAnnotation(
                                AnnotationSpec.builder(SUPPORTED_SOURCE_VERSION_CLASS_NAME)
                                        .addMember("value", "$T.RELEASE_8", SOURCE_VERSION_CLASS_NAME)
                                        .build())
                        .addMethod(process)
                        .addMethod(processAnnotatedElement)
                        .build();
        String currentPackage = this.getClass().getPackage().getName();

        // Put the new type into the output.
        // It provides a service for the Processor class
        into.putType(currentPackage, procSpec, Collections.singletonList(PROCESSOR_CLASS_NAME));

    }
}
