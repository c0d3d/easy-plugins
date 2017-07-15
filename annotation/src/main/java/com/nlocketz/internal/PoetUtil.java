package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;

final class PoetUtil {
    private PoetUtil() {

    }

    static FieldSpec.Builder privateField(Type fType, String name) {
        return FieldSpec.builder(fType, name, Modifier.PRIVATE);
    }

    static FieldSpec.Builder privateField(TypeName type, String name) {
        return FieldSpec.builder(type, name, Modifier.PRIVATE);
    }

    static FieldSpec.Builder privateStaticField(TypeName type, String name) {
        return privateField(type, name).addModifiers(Modifier.STATIC);
    }

    static FieldSpec.Builder privateStaticField(Type type, String name) {
        return privateField(type, name).addModifiers(Modifier.STATIC);
    }

    static MethodSpec.Builder publicMethod(String name, TypeName returnType) {
        return MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC).returns(returnType);
    }

    static MethodSpec.Builder publicStaticMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.STATIC);
    }

    static MethodSpec.Builder publicAbstractMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.ABSTRACT);
    }
}
