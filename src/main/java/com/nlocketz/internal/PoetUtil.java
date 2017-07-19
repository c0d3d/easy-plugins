package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;

final class PoetUtil {
    private PoetUtil() {

    }

    static TypeName wildcardType() {
        return wildcardType(TypeName.OBJECT);
    }

    static TypeName wildcardType(TypeName upperBound) {
        return WildcardTypeName.subtypeOf(upperBound);
    }

    static TypeName wildcardType(Class<?> upperBoundRaw) {
        return WildcardTypeName.subtypeOf(ClassName.get(upperBoundRaw));
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

    static MethodSpec.Builder publicMethod(String name, TypeName returnType) {
        return MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC).returns(returnType);
    }

    static MethodSpec.Builder publicFinalMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.FINAL);
    }

    static MethodSpec.Builder publicAbstractMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.ABSTRACT);
    }

    static MethodSpec.Builder privateMethod(String name, TypeName returnType) {
        return MethodSpec.methodBuilder(name).returns(returnType).addModifiers(Modifier.PRIVATE);
    }
}
