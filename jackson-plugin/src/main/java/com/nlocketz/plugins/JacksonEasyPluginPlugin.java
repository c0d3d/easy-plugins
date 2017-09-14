package com.nlocketz.plugins;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.auto.service.AutoService;
import com.nlocketz.internal.EasyPluginPlugin;
import com.nlocketz.internal.MarkedPluginClass;
import com.nlocketz.internal.UserMarkerAnnotation;
import com.nlocketz.internal.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Iterator;

/**
 * A plugin for easy-plugins which adds the ability to perform
 * Jackson deserialization on plugins.
 */
@AutoService(EasyPluginPlugin.class)
public class JacksonEasyPluginPlugin implements EasyPluginPlugin {
    private static final String DESERIALIZER_CLASS = "Deserializer";
    private static final ParameterizedTypeName ITERATOR_STRING = ParameterizedTypeName.get(Iterator.class, String.class);

    @Override
    public void updateRegistry(TypeSpec.Builder registry, UserMarkerAnnotation annotation) {

        TypeName retType = annotation.getServiceInterfaceTypeName();
        TypeSpec.Builder deserializerBuilder = TypeSpec.classBuilder(DESERIALIZER_CLASS)
                .superclass(ParameterizedTypeName.get(ClassName.get(StdDeserializer.class), retType))
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addModifiers(Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder().addStatement("this(null)").build())
                .addMethod(MethodSpec.constructorBuilder().addParameter(Class.class, "clazz").addStatement("super(clazz)").build());

        String jsonParser = "jsonParser";
        String registeredName = "registeredName";
        String node = "node";
        String fieldNames = "fieldNames";
        MethodSpec deserializerMethod = MethodSpec.methodBuilder("deserialize")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(JsonParser.class, jsonParser)
                .addParameter(DeserializationContext.class, "ctxt")
                .addException(IOException.class)
                .addException(JsonProcessingException.class)
                .returns(annotation.getServiceInterfaceTypeName())
                .addStatement("$T $L", String.class, registeredName)
                .beginControlFlow("if ($L.isExpectedStartObjectToken())", jsonParser)
                .addStatement("$T $L = $L.getCodec().readTree($L)", TreeNode.class, node, jsonParser, jsonParser)
                // Check that object has exactly one key
                .addComment("Check that object has exactly one key")
                .addStatement("$T $L = $L.fieldNames()", ITERATOR_STRING, fieldNames, node)
                .beginControlFlow("if (!$L.hasNext())", fieldNames)
                // No keys; throw an exception
                .addComment("No keys; throw an exception")
                .addStatement(String.format("throw new RuntimeException(\"Failed to deserialize %s: no field names found\")", annotation.getServiceInterfaceName()))
                .endControlFlow()
                // At least one key; get the registered name
                .addComment("At least one key; get the registered name")
                .addStatement("$L = $L.next()", registeredName, fieldNames)
                .beginControlFlow("if ($L.hasNext())", fieldNames)
                .addComment("Too many keys; input is malformed")
                .addStatement(String.format("throw new RuntimeException(\"Failed to deserialize %s: too many field names found\")", annotation.getServiceInterfaceName()))
                .endControlFlow()
                .addComment("Check that service is registered and initialize")
                .beginControlFlow("if (getInstance().$L.containsKey($L))", "pluginMap", registeredName)
                .addStatement("return getInstance().$L.get($L).deserialize($L.get($L).traverse($L.getCodec()))", "pluginMap", registeredName, node, registeredName, jsonParser)
                .nextControlFlow("else")
                .addStatement(String.format("throw new RuntimeException($T.format(\"Failed to deserialize %s: Service \\\"%%s\\\" not found in registry\", $L))",
                        annotation.getServiceInterfaceName()), String.class, registeredName)
                .endControlFlow()
                .nextControlFlow("else")
                .addComment("Not an object; assume we're working with a string that corresponds to a configuration-less service")
                .addStatement("$L = $L.getValueAsString()", registeredName, jsonParser)
                .addComment("Check that service is registered and initialize")
                .beginControlFlow("if (getInstance().$L.containsKey($L))", "pluginMap", registeredName)
                .addStatement("return getInstance().$L.get($L).deserialize()", "pluginMap", registeredName)
                .nextControlFlow("else")
                .addStatement(String.format("throw new RuntimeException($T.format(\"Failed to deserialize %s: Service \\\"%%s\\\" not found in registry\", $L))",
                        annotation.getServiceInterfaceName()), String.class, registeredName)
                .endControlFlow()
                .endControlFlow()
                .build();

        deserializerBuilder = deserializerBuilder.addMethod(deserializerMethod);

        TypeName configType = ParameterizedTypeName.get(ClassName.get(TypeReference.class), WildcardTypeName.subtypeOf(Object.class));
        String serviceName = "serviceName";
        MethodSpec getConfigTypeReference = MethodSpec.methodBuilder("getConfigTypeReference")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(String.class, serviceName)
                .returns(configType)
                .beginControlFlow("if (getInstance().$L.containsKey($L))", "pluginMap", serviceName)
                .addStatement("return getInstance().$L.get($L).getConfigTypeReference()", "pluginMap", serviceName)
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .build();

        registry.addType(deserializerBuilder.build());
        registry.addMethod(getConfigTypeReference);
    }

    @Override
    public void updatePluginProvider(TypeSpec.Builder provider, MarkedPluginClass markedPluginClass) {

        ParameterizedTypeName configTypeRef = ParameterizedTypeName.get(ClassName.get(TypeReference.class), WildcardTypeName.subtypeOf(Object.class));
        MethodSpec.Builder getConfigTypeReferenceBuilder = MethodSpec.methodBuilder("getConfigTypeReference")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(configTypeRef);
        if (markedPluginClass.getConfigType() != null) {
            getConfigTypeReferenceBuilder = getConfigTypeReferenceBuilder
                    .addStatement("return new $T<$T>() {}", TypeReference.class, markedPluginClass.getConfigType());
        } else {
            getConfigTypeReferenceBuilder = getConfigTypeReferenceBuilder
                    .addStatement("return null");
        }

        String config = "config";
        String typeRef = "typeRef";
        MethodSpec deserializeWithConfig = Util.publicFinalMethod("deserialize", markedPluginClass.getTypeName())
                .addAnnotation(Override.class)
                .addParameter(JsonParser.class, config)
                .addStatement("$T $L = this.getConfigTypeReference()", configTypeRef, typeRef)
                .beginControlFlow("if ($L == null || $L == null)", config, typeRef)
                .addStatement("return this.create()")
                .nextControlFlow("else")
                .beginControlFlow("try")
                .addStatement("return this.createWithConfig($L.readValueAs($L))", config, typeRef)
                .nextControlFlow("catch ($T e)", IOException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .endControlFlow()
                .build();

        MethodSpec deserializeWithoutConfig = Util.publicFinalMethod("deserialize", markedPluginClass.getTypeName())
                .addAnnotation(Override.class)
                .addStatement("return this.create()")
                .build();

        provider.addMethod(getConfigTypeReferenceBuilder.build());
        provider.addMethod(deserializeWithConfig);
        provider.addMethod(deserializeWithoutConfig);
    }

    @Override
    public void updatePluginProviderInterface(TypeSpec.Builder serviceInterface, UserMarkerAnnotation annotation) {
        TypeName returnType = annotation.getServiceInterfaceTypeName();
        MethodSpec deserializeWithoutConfig = Util.publicAbstractMethod("deserialize", returnType)
                .build();
        MethodSpec deserializeWithConfig = Util.publicAbstractMethod("deserialize", returnType)
                .addParameter(JsonParser.class, "config")
                .build();
        TypeName configType = ParameterizedTypeName.get(ClassName.get(TypeReference.class), WildcardTypeName.subtypeOf(Object.class));
        MethodSpec getConfigTypeReference = Util.publicAbstractMethod("getConfigTypeReference", configType).build();

        serviceInterface.addMethod(deserializeWithoutConfig);
        serviceInterface.addMethod(deserializeWithConfig);
        serviceInterface.addMethod(getConfigTypeReference);
    }
}
