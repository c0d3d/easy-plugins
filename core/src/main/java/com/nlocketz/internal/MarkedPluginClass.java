package com.nlocketz.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MarkedPluginClass {

    private Map<Integer, Set<List<TypeMirror>>> constructorSignatures;
    private Map<List<TypeMirror>, Element> constructorElements;
    private TypeElement clazzElement;
    private final Elements elements;
    private final Types types;

    /**
     * The statement used to create the default constructor
     * Note: {@link #constWithMap} or defaultConstString will be null, but not both.
     */
    private String defaultConstString;
    /**
     * The statement used to create calls to the map constructor
     */
    private String constWithMap;

    private String serviceName;

    MarkedPluginClass(TypeElement clazzElement,
                      String serviceName,
                      Types types,
                      Elements elements) {

        this.clazzElement = clazzElement;
        this.constructorSignatures = new HashMap<>();
        this.constructorElements = new IdentityHashMap<>();
        this.elements = elements;
        this.types = types;
        computeConstructors(clazzElement);

        boolean hasEither = false;
        // Check for no-argument constructor
        if (this.hasConstructor()) {
            hasEither = true;
            defaultConstString = "return new " + clazzElement.getQualifiedName() + "()";
        }

        // Check for constructor which takes a map
        if (this.hasConstructor(Map.class)) {
            hasEither = true;
            constWithMap = "return new " + clazzElement.getQualifiedName() + "(%s)";
        }

        if (!hasEither) {
            throw new EasyPluginException(
                    "Services must have either a default constructor, or one taking in java.util.Map<String,String>");
        }

        this.serviceName = serviceName;

    }

    /**
     * Gets the {@link TypeName} for this marked service class
     * @return The typename
     */
    public TypeName getTypeName() {
        return TypeName.get(clazzElement.asType());
    }

    /**
     * Extracts the constructors from the given class
     * @param clazzElement The {@link TypeElement} corresponding to the class
     */
    private void computeConstructors(TypeElement clazzElement) {

        for (Element e : clazzElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) e;
                List<? extends VariableElement> params = ee.getParameters();
                List<TypeMirror> signature = new ArrayList<>(params.size());
                for (VariableElement variableElement : params) {
                    signature.add(this.types.erasure(variableElement.asType()));
                }
                int arity = signature.size();
                if (!this.constructorSignatures.containsKey(arity)) {
                    this.constructorSignatures.put(arity, new HashSet<List<TypeMirror>>());
                }
                this.constructorSignatures.get(arity).add(signature);
                this.constructorElements.put(signature, e);
            }
        }
    }

    /**
     * Checks whether this class has a constructor with the given signature,
     * and returns the corresponding constructor element if it exists.
     * The given {@code names} can each have any of the following types
     * (or any of their subclasses):
     * <ul>
     *     <li>{@link TypeName}</li>
     *     <li>{@link Type}</li>
     *     <li>{@link Class}</li>
     *     <li>{@link String} (containing a fully-qualified class name; this should be available on the RUNNING classpath)</li>
     * </ul>
     * @param names The signature of the constructor
     * @return The element corresponding to the constructor with the given signature, if any
     */
    public Element getConstructor(Object... names) {
        // FIXME: Erasure should be handled better here
        List<TypeMirror> signature = new ArrayList<>();
        for (Object name : names) {
            signature.add(this.toTypeMirror(name));
        }
        Set<List<TypeMirror>> constructorSignaturesWithArity = this.constructorSignatures.get(signature.size());
        if (constructorSignaturesWithArity == null) {
            return null;
        }
        for (List<TypeMirror> constructorSignature : constructorSignaturesWithArity) {
            boolean ret = true;
            for (int idx = 0; idx < constructorSignature.size(); ++idx) {
                TypeMirror signatureArg = signature.get(idx);
                TypeMirror constructorArg = constructorSignature.get(idx);
                if (!this.types.isAssignable(signatureArg, constructorArg)) {
                    ret = false;
                    break;
                }
            }
            if (ret) {
                return this.constructorElements.get(constructorSignature);
            }
        }
        return null;
    }

    /**
     * Checks whether this class has a constructor with the given signature.
     * The given {@code names} can each have any of the following types
     * (or any of their subclasses):
     * <ul>
     *     <li>{@link TypeName}</li>
     *     <li>{@link Type}</li>
     *     <li>{@link Class}</li>
     *     <li>{@link String} (containing a fully-qualified class name; this should be available on the RUNNING classpath)</li>
     * </ul>
     * This method is equivalent to the following:
     * <pre>
     *     getConstructor(names) != null
     * </pre>
     * @param names The signature of the constructor
     * @return Whether this marked class has a constructor with the given signature
     */
    public boolean hasConstructor(Object... names) {
        return this.getConstructor(names) != null;
    }

    private TypeMirror toTypeMirror(Object name) {
        if (name instanceof TypeMirror) {
            return (TypeMirror)name;
        } else if (name instanceof Class) {
            return this.elements.getTypeElement(((Class)name).getCanonicalName()).asType();
        } else if (name instanceof String) {
            return this.elements.getTypeElement((String) name).asType();
        } else if (name instanceof ClassName) {
            ClassName clazz = (ClassName)name;
            StringBuilder classNameBuilder = new StringBuilder();
            classNameBuilder.append(clazz.packageName());
            for (String elt : clazz.simpleNames()) {
                classNameBuilder.append(".");
                classNameBuilder.append(elt);
            }
            return this.elements.getTypeElement(classNameBuilder.toString()).asType();
        } else if (name instanceof ParameterizedTypeName) {
            return this.toTypeMirror(((ParameterizedTypeName)name).rawType);
        } else if (name instanceof TypeName) {
            return this.elements.getTypeElement(((TypeName)name).withoutAnnotations().toString()).asType();
        } else {
            throw new IllegalArgumentException(String.format("Invalid type name: %s", name));
        }
    }

    /**
     * Adds a call to the configuration constructor, if one exists.
     * Otherwise a default constructor call will be added.
     * @param getByNameBuilder The builder to add to.
     * @param mapName The name of the configuration map in scope.
     */
    public void addMapConstructorCall(MethodSpec.Builder getByNameBuilder, String mapName) {
        if (constWithMap != null) {
            getByNameBuilder.addStatement(String.format(constWithMap, "$L"), mapName);
        } else {
            // Note: This won't be null bc of invariant
            getByNameBuilder.addStatement(defaultConstString);
        }
    }

    /**
     * Adds a call to the default constructor for this marked service class to the given method builder.
     * If the "default" means an empty map, one will be supplied.
     * @param getByNameBuilder The builder to add the constructions to.
     */
    public void addDefaultConstructorCall(MethodSpec.Builder getByNameBuilder) {
        if (defaultConstString != null) {
            getByNameBuilder.addStatement(defaultConstString);
        } else {
            getByNameBuilder.addStatement(String.format(constWithMap, "$T.<String,String>emptyMap()"), Collections.class);
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getNewServiceClassName() {
        return clazzElement.getQualifiedName()
                .toString().replace('.','$') + "$Service$" + serviceName.replace("\"", "");
    }

    public TypeElement getAnnotatedEle() {
        return clazzElement;
    }
}
