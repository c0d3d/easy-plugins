package com.nlocketz.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static javax.lang.model.element.ElementKind.*;

public final class Util {

    private static final ServiceLoader<EasyPluginPlugin> pluginLoader = ServiceLoader.load(EasyPluginPlugin.class, Util.class.getClassLoader());

    private Util() {

    }

    public static TypeName wildcardType() {
        return wildcardType(TypeName.OBJECT);
    }

    public static TypeName wildcardType(TypeName upperBound) {
        return WildcardTypeName.subtypeOf(upperBound);
    }

    public static TypeName wildcardType(Class<?> upperBoundRaw) {
        return WildcardTypeName.subtypeOf(ClassName.get(upperBoundRaw));
    }

    public static FieldSpec.Builder privateField(Type fType, String name) {
        return FieldSpec.builder(fType, name, Modifier.PRIVATE);
    }

    public static FieldSpec.Builder privateField(TypeName type, String name) {
        return FieldSpec.builder(type, name, Modifier.PRIVATE);
    }

    public static FieldSpec.Builder privateStaticField(TypeName type, String name) {
        return privateField(type, name).addModifiers(Modifier.STATIC);
    }

    public static MethodSpec.Builder publicMethod(String name, TypeName returnType) {
        return MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC).returns(returnType);
    }

    public static MethodSpec.Builder publicFinalMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.FINAL);
    }

    public static MethodSpec.Builder publicAbstractMethod(String name, TypeName returnType) {
        return publicMethod(name, returnType).addModifiers(Modifier.ABSTRACT);
    }

    public static MethodSpec.Builder privateMethod(String name, TypeName returnType) {
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
                    writer.write("\n");
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

    private static boolean isNestedClass(Element element) {
        Element enclosing = element.getEnclosingElement();
        return enclosing != null && enclosing.getKind().isClass();
    }



    /**
     * Checks that {@code target} is visible from {@code fromPkg}.
     * If {@code fromPkg} is {@code null}, we take that to mean that {@code target} should be visible everywhere.
     * Throws an {@link EasyPluginException} with a proper error message if the target element does not match
     * the visibility constraint.
     * @param eles Elements
     * @param target The target element to check for visibility
     * @param fromPkg The package to check for visibility from.
     *                Null indicates it needs to be globally visible.
     * @throws EasyPluginException if it's not visible
     */
    static void checkElementVisibility(Elements eles, Element target, String fromPkg) {
        // I would have used a switch, but that messed up compilation somehow.
        // I guess it generated another class file?
        // Anyways, this works.
        if (target.getKind().isClass() || target.getKind().isInterface()) {
            checkClassVisibility(eles, (TypeElement) target, fromPkg);
        } else if (target.getKind().isField()
                || target.getKind() == ElementKind.METHOD
                || target.getKind() == ElementKind.CONSTRUCTOR) {
            checkMemberVisibility(eles, target, fromPkg);
        } else if (target.getKind() == ElementKind.ENUM_CONSTANT) {
            checkClassVisibility(eles, (TypeElement) target.getEnclosingElement(), fromPkg);
        } else {
            // This isn't an EasyPluginException because our code shouldn't be dumb
            // enough to check the visibility of any other kind of element.
            throw new IllegalArgumentException("Bad kind for element visibility check: " + target.getKind());
        }
    }

    private static void simpleVisibility(Set<Modifier> targetMods,
                                         PackageElement targetPkg,
                                         String fromPkg,
                                         Element target) {
        if (fromPkg == null) {
            if (!targetMods.contains(Modifier.PUBLIC)) {
                throw new EasyPluginException(target.toString() + " must be public.");
            }
        } else {
            // TODO this is terrible
            if ((!targetPkg.getQualifiedName().toString().equals(fromPkg)
                    || targetMods.contains(Modifier.PROTECTED)
                    || targetMods.contains(Modifier.PRIVATE))
                    && !targetMods.contains(Modifier.PUBLIC)) {
                throw new EasyPluginException("Access modifiers block usage of " + target.toString());
            }
        }
    }

    private static void checkMemberVisibility(Elements eles, Element target, String fromPkg) {

        // Obviously the surrounding type needs to be visible
        checkClassVisibility(eles, (TypeElement)target.getEnclosingElement(), fromPkg);

        PackageElement pe = eles.getPackageOf(target);

        // Check the member's visibility
        simpleVisibility(target.getModifiers(), pe, fromPkg, target);
    }

    private static void checkClassVisibility(Elements eles, TypeElement target, String fromPkg) {
        Set<Modifier> targetMods = target.getModifiers();
        if (!isNestedClass(target)) {
            simpleVisibility(target.getModifiers(), eles.getPackageOf(target), fromPkg, target);
        } else {
            Element surround = target.getEnclosingElement();

            if (surround.getKind() != CLASS
                    && surround.getKind() != INTERFACE
                    // Not sure if that is allowed, but we include it anyways.
                    && surround.getKind() != ENUM) {
                throw new IllegalStateException(
                        target.toString() + " is not nested? " + surround.toString());
            }

            TypeElement enclosingType = (TypeElement) surround;

            // The surrounding class must be visible for us to see the nested class
            if (!isElementVisibleFrom(eles, enclosingType, fromPkg)) {
                throw new EasyPluginException(enclosingType.toString()
                        + " is not visible from output package; need for access to " + target.toString());
            }
            // Nested class must be static since we don't have an enclosing instance
            if (!targetMods.contains(Modifier.STATIC)) {
                throw new EasyPluginException(target.toString() + " must be static.");
            }

            simpleVisibility(target.getModifiers(), eles.getPackageOf(target), fromPkg, target);
        }
    }

    /**
     * Same as {@link #checkElementVisibility(Elements, Element, String)}, but returns the result instead of
     * throwing an exception.
     * @param eles Elements
     * @param target The target element to check for visibility
     * @param from The package to check from, null means the target element should be globally visible.
     * @return {@code true} if the element matches the visibility constraint imposed by {@code from}.
     * @see #checkElementVisibility(Elements, Element, String)
     */
    private static boolean isElementVisibleFrom(Elements eles, Element target, String from) {
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

    static ServiceLoader<EasyPluginPlugin> getPluginLoader() {
        return pluginLoader;
    }
}
