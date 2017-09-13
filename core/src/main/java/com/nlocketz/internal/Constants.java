package com.nlocketz.internal;

import com.nlocketz.ConfigurationConstructor;
import com.nlocketz.Service;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.net.URL;
import java.util.*;

public final class Constants {

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
    static final String PLUGIN_MAP = "pluginMap";

    /* These are only used to construct generic types in here */
    static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);
    static final ClassName MAP_CLASS_NAME = ClassName.get(Map.class);
    static final ClassName HASHMAP_CLASS_NAME = ClassName.get(HashMap.class);
    static final ClassName SET_CLASS_NAME = ClassName.get(Set.class);

    /* Class names used */
    static final ClassName TYPE_ELEMENT_CLASS_NAME = ClassName.get(TypeElement.class);
    static final ClassName ELEMENT_CLASS_NAME = ClassName.get(Element.class);
    static final ClassName EZ_SERVICE_EXCEPTION_CLASS_NAME = ClassName.get(EasyPluginException.class);
    static final ClassName ROUND_ENV_CLASS_NAME = ClassName.get(RoundEnvironment.class);
    static final ClassName COMPLETE_SERVICE_BUILDER_CLASS_NAME = ClassName.get(CompletePluginGenerator.class);
    static final ClassName ABSTRACT_PROCESSOR_CLASS_NAME = ClassName.get(AbstractProcessor.class);
    static final ClassName SUPPORTED_ANNOTATION_TYPES_CLASS_NAME = ClassName.get(SupportedAnnotationTypes.class);
    static final ClassName SUPPORTED_SOURCE_VERSION_CLASS_NAME = ClassName.get(SupportedSourceVersion.class);
    static final ClassName SOURCE_VERSION_CLASS_NAME = ClassName.get(SourceVersion.class);
    static final ClassName PROCESSOR_CLASS_NAME = ClassName.get(Processor.class);
    static final ClassName MARKER_ANNOTATION_CLASS_NAME = ClassName.get(MarkerAnnotation.class);
    static final ClassName SERVICE_LOADER_CLASS_NAME = ClassName.get(ServiceLoader.class);
    static final ClassName PROC_OUT_COLL_CLASS_NAME = ClassName.get(ProcessorOutputCollection.class);

    /* Type names */
    static final TypeName STRING_TYPE_NAME = TypeName.get(String.class);
    static final ParameterizedTypeName MAP_STRING_STRING_NAME =
            ParameterizedTypeName.get(MAP_CLASS_NAME, STRING_CLASS_NAME, STRING_CLASS_NAME);
    static final ParameterizedTypeName SET_WILD_EXTENDS_ELE =
            ParameterizedTypeName.get(SET_CLASS_NAME, Util.wildcardType(TYPE_ELEMENT_CLASS_NAME));
    public static final TypeName CONFIG_TYPE_NAME = TypeName.get(Object.class);

    /* All names of methods */
    static final String GET_NAME_METHOD_NAME = "getProviderName";
    static final String CREATE_NEW_METHOD_NAME = "create";
    static final String CREATE_NEW_WITH_CONFIG_METHOD_NAME = "createWithConfig";

    public static URL[] classesToCopy = new URL[]{
            AbstractPluginFileGenerator.class.getResource("AbstractPluginFileGenerator.class"),
            CompletePluginGenerator.class.getResource("CompletePluginGenerator.class"),
            EasyPluginException.class.getResource("EasyPluginException.class"),
            Constants.class.getResource("Constants.class"),
            MarkedPluginClass.class.getResource("MarkedPluginClass.class"),
            MarkerAnnotation.class.getResource("MarkerAnnotation.class"),
            PluginAnnotation.class.getResource("PluginAnnotation.class"),
            PluginAnnotationProcessorGenerator.class.getResource("PluginAnnotationProcessorGenerator.class"),
            PluginFileGenerator.class.getResource("PluginFileGenerator.class"),
            PluginProviderFileGenerator.class.getResource("PluginProviderFileGenerator.class"),
            PluginProviderInterfaceFileGenerator.class.getResource("PluginProviderInterfaceFileGenerator.class"),
            PluginRegistryFileGenerator.class.getResource("PluginRegistryFileGenerator.class"),
            ProcessorOutputCollection.class.getResource("ProcessorOutputCollection.class"),
            UserMarkerAnnotation.class.getResource("UserMarkerAnnotation.class"),
            EasyPluginPlugin.class.getResource("EasyPluginPlugin.class"),
            Util.class.getResource("Util.class"),
            ParameterSpec.class.getResource("ParameterSpec$1.class"),
            CodeBlock.class.getResource("CodeBlock$1.class"),
            TypeName.class.getResource("TypeName.class"),
            TypeName.class.getResource("LineWrapper.class"),
            JavaFile.class.getResource("JavaFile.class"),
            AnnotationSpec.class.getResource("AnnotationSpec$1.class"),
            JavaFile.class.getResource("JavaFile$2.class"),
            WildcardTypeName.class.getResource("WildcardTypeName.class"),
            AnnotationSpec.class.getResource("AnnotationSpec.class"),
            CodeBlock.class.getResource("CodeBlock.class"),
            CodeBlock.class.getResource("CodeWriter.class"),
            JavaFile.class.getResource("JavaFile$1.class"),
            ArrayTypeName.class.getResource("ArrayTypeName.class"),
            TypeSpec.class.getResource("TypeSpec$Builder.class"),
            TypeSpec.class.getResource("Util.class"),
            FieldSpec.class.getResource("FieldSpec.class"),
            TypeName.class.getResource("TypeName$1.class"),
            CodeBlock.class.getResource("CodeBlock$Builder.class"),
            FieldSpec.class.getResource("FieldSpec$Builder.class"),
            TypeSpec.class.getResource("TypeSpec$Kind.class"),
            FieldSpec.class.getResource("FieldSpec$1.class"),
            MethodSpec.class.getResource("MethodSpec.class"),
            MethodSpec.class.getResource("MethodSpec$Builder.class"),
            TypeSpec.class.getResource("TypeSpec.class"),
            TypeName.class.getResource("TypeName$2.class"),
            TypeSpec.class.getResource("TypeSpec$1.class"),
            NameAllocator.class.getResource("NameAllocator.class"),
            AnnotationSpec.class.getResource("AnnotationSpec$Visitor.class"),
            JavaFile.class.getResource("JavaFile$Builder.class"),
            MethodSpec.class.getResource("MethodSpec$1.class"),
            ParameterSpec.class.getResource("ParameterSpec.class"),
            ParameterSpec.class.getResource("ParameterSpec$Builder.class"),
            TypeVariableName.class.getResource("TypeVariableName.class"),
            ClassName.class.getResource("ClassName.class"),
            ParameterizedTypeName.class.getResource("ParameterizedTypeName.class"),
            AnnotationSpec.class.getResource("AnnotationSpec$Builder.class"),
            Service.class.getResource("Service.class"),
            ConfigurationConstructor.class.getResource("ConfigurationConstructor.class")
    };
}
