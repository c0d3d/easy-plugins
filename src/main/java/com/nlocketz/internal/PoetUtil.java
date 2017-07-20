package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Modifier;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static Set<String> readEntries(InputStream is) throws IOException {

        Set<String> entries = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
        }
        return entries;
    }

    static void writeMetaInfServices(String spInterface, Set<String> processorQNames, Filer filer) {
        Set<String> content = new HashSet<>();
        String servicesFilePath = "META-INF/services/" + spInterface;

        try {
            // This is supposed to throw FilerException if the resource doesn't exist
            // But I guess sometimes it hands back a dummy FileObject so you have to try and read from it.
            // If the read fails then it is a dummy.
            FileObject current = filer.getResource(StandardLocation.CLASS_OUTPUT, "", servicesFilePath);
            content = readEntries(current.openInputStream());
        } catch (IOException e) {
            // It's a dummy
        }

        try {
            FileObject newRes = filer.createResource(StandardLocation.CLASS_OUTPUT, "", servicesFilePath);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(newRes.openOutputStream()))) {
                for (String s : content) {
                    writer.write(s);
                }
                for (String s : processorQNames) {
                    writer.append(s);
                    writer.append("\n");
                }
            }
        } catch (FilerException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
