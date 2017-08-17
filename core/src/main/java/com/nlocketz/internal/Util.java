package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Util {
    private Util() {

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

    static String packageNameFromQName(String s) {
        return s.substring(0, s.lastIndexOf('.'));
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static AnnotationMirror getMatchingMirror(TypeMirror serviceAnnotationMirror,
                                               List<? extends AnnotationMirror> mirrors,
                                               Types t) {

        for (AnnotationMirror mirror : mirrors) {
            if (t.isSameType(mirror.getAnnotationType().asElement().asType(), serviceAnnotationMirror)) {
                return mirror;
            }
        }
        return null;
    }

    static AnnotationValue getValueByName(AnnotationMirror mirror, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                mirror.getElementValues().entrySet()) {
            ExecutableElement exec = entry.getKey();
            if (exec.getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    static String getValueStringByName(AnnotationMirror mirror, String name) {
        AnnotationValue val = getValueByName(mirror, name);
        if (val == null) {
            return null;
        } else {
            return val.getValue().toString();
        }
    }

    private static boolean isNestedClass(TypeElement element) {
        Element enclosing = element.getEnclosingElement();
        return enclosing != null && enclosing.getKind().isClass();
    }

    private static void checkElementVisibilityNoNest(Elements eles, TypeElement target, String from) {
        Set<Modifier> targetMods = target.getModifiers();
        PackageElement targetPkg = eles.getPackageOf(target);

        // TODO this is terrible
        if ((!targetPkg.getQualifiedName().toString().equals(from)
                || targetMods.contains(Modifier.PROTECTED)
                || targetMods.contains(Modifier.PRIVATE))
                && !targetMods.contains(Modifier.PUBLIC)) {
            throw new EasyPluginException("Access modifiers block usage of " + target.toString());
        }
    }

    static void checkElementVisibility(Elements eles, TypeElement target, String from) {
        Set<Modifier> targetMods = target.getModifiers();
        if (!isNestedClass(target)) {
            checkElementVisibilityNoNest(eles, target, from);
        } else {
            Element surround = target.getEnclosingElement();

            if (surround.getKind() != ElementKind.CLASS
                    && surround.getKind() != ElementKind.INTERFACE) {
                throw new IllegalStateException("?!?!: "+surround.getKind());
            }

            TypeElement enclosingType = (TypeElement) surround;

            // The surrounding class must be visible for us to see the nested class
            if (!isElementVisibleFrom(eles, enclosingType, from)) {
                throw new EasyPluginException(enclosingType.toString()
                        + " is not visible from output package; need for access to " + target.toString());
            }
            // Nested class must be static since we don't have an enclosing instance
            if (!targetMods.contains(Modifier.STATIC)) {
                throw new EasyPluginException(target.toString() + " must be static to get service instance.");
            }

            checkElementVisibilityNoNest(eles, target, from);
        }
    }

    private static boolean isElementVisibleFrom(Elements eles, TypeElement target, String from) {
        try {
            checkElementVisibility(eles, target, from);
            return true;
        } catch (EasyPluginException e) {
            return false;
        }
    }

    static void checkConcreteType(TypeElement e) {
        Set<Modifier> mods = e.getModifiers();
        if (mods.contains(Modifier.ABSTRACT) || e.getKind().isInterface()) {
            throw new EasyPluginException(e.toString() + " must be concrete to use as a service.");
        }
    }
}
