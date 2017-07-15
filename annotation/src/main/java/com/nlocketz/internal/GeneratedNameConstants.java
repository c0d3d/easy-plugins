package com.nlocketz.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Map;

final class GeneratedNameConstants {

    static final String INSTANCE_FIELD_NAME = "instance";
    static final String SERVICE_LOADER_FIELD_NAME = "loader";
    static final String PROVIDER_NAME_ARG_NAME = "name";
    static final String CONFIG_NAME_ARG_NAME = "config";

    static final ClassName STRING_CLASS_NAME = ClassName.get(String.class);
    static final TypeName STRING_TYPE_NAME = TypeName.get(String.class);
    static final ClassName MAP_CLASS_NAME = ClassName.get(Map.class);
    static final ParameterizedTypeName MAP_STRING_STRING_NAME =
            ParameterizedTypeName.get(MAP_CLASS_NAME, STRING_CLASS_NAME, STRING_CLASS_NAME);
    static final String GET_NAME_METHOD_NAME = "getProviderName";
    static final String CREATE_NEW_METHOD_NAME = "create";
    static final String CREATE_NEW_WITH_CONFIG_METHOD_NAME = "createWithConfig";
    static final String CONFIG_ARG_NAME = "config";

}
