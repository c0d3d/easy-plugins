package com.nlocketz.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

final class GeneratedNameConstants {

    /* All names of fields, or method argument names */
    static final String INSTANCE_FIELD_NAME = "instance";
    static final String SERVICE_LOADER_FIELD_NAME = "loader";
    static final String PROVIDER_NAME_ARG_NAME = "name";
    static final String CONFIG_NAME_ARG_NAME = "config";
    static final String CONFIG_ARG_NAME = "config";
    static final String PROCESS_METHOD_ARG_SET_NAME = "set";
    static final String ROUND_ENV_NAME = "roundEnv";
    static final String PROCESSING_ENV_NAME = "processingEnv";
    static final String PROCESS_ANNOTATED_ELEMENT_ELEMENT_ARG_NAME = "element";
    static final String PROCESS_ANNOTATED_ELEMENT_METHOD_NAME = "processAnnotatedElement";
    static final String WRITE_FILE_METHOD_NAME = "writeFile";

    /* These are only used to construct generic types in here */
    static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);
    static final ClassName MAP_CLASS_NAME = ClassName.get(Map.class);
    static final ClassName LIST_CLASS_NAME = ClassName.get(List.class);
    static final ClassName SET_CLASS_NAME = ClassName.get(Set.class);

    /* Class names used */
    static final ClassName TYPE_ELEMENT_CLASS_NAME = ClassName.get(TypeElement.class);
    static final ClassName ELEMENT_CLASS_NAME = ClassName.get(Element.class);
    static final ClassName EZ_SERVICE_EXCEPTION_CLASS_NAME = ClassName.get(EasyPluginException.class);
    static final ClassName ROUND_ENV_CLASS_NAME = ClassName.get(RoundEnvironment.class);
    static final ClassName COMPLETE_SERVICE_BUILDER_CLASS_NAME = ClassName.get(CompletePluginGenerator.class);
    static final ClassName JAVA_FILE_CLASS_NAME = ClassName.get(JavaFile.class);
    static final ClassName ABSTRACT_PROCESSOR_CLASS_NAME = ClassName.get(AbstractProcessor.class);
    static final ClassName SUPPORTED_ANNOTATION_TYPES_CLASS_NAME = ClassName.get(SupportedAnnotationTypes.class);
    static final ClassName SUPPORTED_SOURCE_VERSION_CLASS_NAME = ClassName.get(SupportedSourceVersion.class);
    static final ClassName SOURCE_VERSION_CLASS_NAME = ClassName.get(SourceVersion.class);
    static final ClassName PROCESSOR_CLASS_NAME = ClassName.get(Processor.class);
    static final ClassName FILER_EXCEPTION_CLASS_NAME = ClassName.get(FilerException.class);
    static final ClassName MARKER_ANNOTATION_CLASS_NAME = ClassName.get(MarkerAnnotation.class);
    static final ClassName SERVICE_LOADER_CLASS_NAME = ClassName.get(ServiceLoader.class);
    static final ClassName PROC_OUT_COLL_CLASS_NAME = ClassName.get(ProcessorOutputCollection.class);

    /* Type names */
    static final TypeName STRING_TYPE_NAME = TypeName.get(String.class);
    static final ParameterizedTypeName MAP_STRING_STRING_NAME =
            ParameterizedTypeName.get(MAP_CLASS_NAME, STRING_CLASS_NAME, STRING_CLASS_NAME);
    static final ParameterizedTypeName LIST_JAVA_FILE_NAME =
            ParameterizedTypeName.get(LIST_CLASS_NAME, JAVA_FILE_CLASS_NAME);
    static final ParameterizedTypeName SET_WILD_EXTENDS_ELE =
            ParameterizedTypeName.get(SET_CLASS_NAME, Util.wildcardType(TYPE_ELEMENT_CLASS_NAME));

    /* All names of methods */
    static final String GET_NAME_METHOD_NAME = "getProviderName";
    static final String CREATE_NEW_METHOD_NAME = "create";
    static final String CREATE_NEW_WITH_CONFIG_METHOD_NAME = "createWithConfig";

}
