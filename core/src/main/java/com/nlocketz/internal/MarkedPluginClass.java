package com.nlocketz.internal;

import com.nlocketz.ConfigurationConstructor;
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

    private Map<Integer, Set<List<TypeMirror>>> constructorArities;
    private Map<List<TypeMirror>, Element> constructorElements;
    private Map<Element, List<TypeMirror>> constructorSignatures;
    private TypeElement clazzElement;
    private Element configConstructor;
    private TypeMirror configType;
    private final Elements elements;
    private final Types types;

    /**
     * The statement used to create the default constructor
     * Note: {@link #constWithConfig} or defaultConstString will be null, but not both.
     */
    private String defaultConstString;
    /**
     * The statement used to create calls to the configuration constructor
     */
    private String constWithConfig;

    private String serviceName;

    MarkedPluginClass(TypeElement clazzElement,
                      String serviceName,
                      Types types,
                      Elements elements) {

        this.clazzElement = clazzElement;
        this.constructorArities = new HashMap<>();
        this.constructorElements = new IdentityHashMap<>();
        this.constructorSignatures = new IdentityHashMap<>();
        this.elements = elements;
        this.types = types;
        computeConstructors(clazzElement);

        boolean hasEither = false;
        // Check for no-argument constructor
        if (this.hasConstructor()) {
            hasEither = true;
            defaultConstString = "return new " + clazzElement.getQualifiedName() + "()";
        }

        // Check for constructor which takes a configuration
        if (this.configConstructor != null) {
            int arity = ((ExecutableElement)this.configConstructor).getParameters().size();
            if (arity > 1) {
                throw new EasyPluginException(String.format("Error in class %s. Constructors annotated with @ConfigurationConstructor should only have zero or one arguments",
                        clazzElement.getSimpleName()));
            }
            hasEither = true;
            if (arity == 0) {
                if (defaultConstString == null) {
                    defaultConstString = "return new " + clazzElement.getQualifiedName() + "()";
                }
                // For the sake of simplicity, we assume in the rest of the file that configConstructor takes a single argument.
                this.configConstructor = null;
            } else {
                constWithConfig = "return new " + clazzElement.getQualifiedName() + "(($T)%s)";
            }
        } else if (this.hasConstructorWithCast(Object.class)) {
            // Check that we have at most one single-argument constructor
            // (since none are marked with @ConfigurationConstructor)
            if (this.constructorArities.get(1).size() != 1) {
                throw new EasyPluginException(String.format("Ambiguous single-argument constructors. Class %s should only have one constructor or annotate one with @ConfigurationConstructor.",
                        clazzElement.getSimpleName()));
            }
            // We know that the size is at least one, since we're in this `if` branch
            this.configConstructor = this.constructorElements.get(this.constructorArities.get(1).iterator().next());
            hasEither = true;
            constWithConfig = "return new " + clazzElement.getQualifiedName() + "(($T)%s)";
        }

        // Get type of configuration
        if (this.configConstructor != null) {
            this.configType = this.constructorSignatures.get(this.configConstructor).get(0);
        }

        if (!hasEither) {
            throw new EasyPluginException(
                    "Services must have either a default constructor, or one taking in a single argument");
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
     * Gets the {@link TypeMirror} for the configuration parameter for this class.
     * If no configuration constructor is available, {@code null} is returned.
     * @return The configuration type, if any
     */
    public TypeMirror getConfigType() {
        return configType;
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
                List<TypeMirror> trueSignature = new ArrayList<>(params.size());
                for (VariableElement variableElement : params) {
                    trueSignature.add(variableElement.asType());
                    signature.add(this.types.erasure(variableElement.asType()));
                }
                int arity = signature.size();
                if (!this.constructorArities.containsKey(arity)) {
                    this.constructorArities.put(arity, new HashSet<List<TypeMirror>>());
                }
                this.constructorArities.get(arity).add(signature);
                this.constructorElements.put(signature, e);
                this.constructorSignatures.put(e, trueSignature);
                if (e.getAnnotation(ConfigurationConstructor.class) != null) {
                    this.configConstructor = e;
                }
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
        Set<List<TypeMirror>> constructorSignaturesWithArity = this.constructorArities.get(signature.size());
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
     * Like {@link #getConstructor(Object...)}, but also matches constructors which are
     * compatible with the given signature via casts.
     * @param names The signature of the constructor
     * @return The element corresponding to the constructor with the given signature, if any
     */
    public Element getConstructorWithCast(Object... names) {
        // FIXME: Erasure should be handled better here
        List<TypeMirror> signature = new ArrayList<>();
        for (Object name : names) {
            signature.add(this.toTypeMirror(name));
        }
        Set<List<TypeMirror>> constructorSignaturesWithArity = this.constructorArities.get(signature.size());
        if (constructorSignaturesWithArity == null) {
            return null;
        }
        for (List<TypeMirror> constructorSignature : constructorSignaturesWithArity) {
            boolean ret = true;
            for (int idx = 0; idx < constructorSignature.size(); ++idx) {
                TypeMirror signatureArg = signature.get(idx);
                TypeMirror constructorArg = constructorSignature.get(idx);
                if (!this.types.isAssignable(signatureArg, constructorArg) && !this.types.isAssignable(constructorArg, signatureArg)) {
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

    /**
     * Like {@link #hasConstructor(Object...)}, but also returns true if there is a
     * constructor which the given signature can be cast to.
     * @param names The signature of the constructor
     * @return Whether this marked class has a constructor with the given signature
     */
    public boolean hasConstructorWithCast(Object... names) {
        return this.getConstructorWithCast(names) != null;
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
     * @param configName The name of the configuration map in scope.
     */
    public void addMapConstructorCall(MethodSpec.Builder getByNameBuilder, String configName) {
        if (constWithConfig != null) {
            getByNameBuilder.addStatement(String.format(constWithConfig, "$L"), configType, configName);
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
        } else if (types.isSameType(types.erasure(toTypeMirror(Map.class)), types.erasure(configType))) {
            // Special case: we call default constructors which take a map with an empty map instead of null
            getByNameBuilder.addStatement(String.format(constWithConfig, "(($T)$T.emptyMap())"), configType, Map.class, Collections.class);
        } else if (types.isSameType(types.erasure(toTypeMirror(List.class)), types.erasure(configType))) {
            // Special case: we call default constructors which take a list with an empty list instead of null
            getByNameBuilder.addStatement(String.format(constWithConfig, "(($T)$T.emptyList())"), configType, List.class, Collections.class);
        } else {
            getByNameBuilder.addStatement(String.format(constWithConfig, getDefaultValue()), configType);
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

    // Returns the default value for this class's configuration type
    private String getDefaultValue() {
        if (this.configType == null) {
            throw new IllegalStateException("Internal error: Should not be called with null configuration type");
        }
        TypeName configType = TypeName.get(types.erasure(this.configType));
        // Fast case: non-primitives
        if (!configType.isPrimitive()) {
            return "null";
        }
        // This is a primitive. Return the appropriate value
        if (configType.equals(TypeName.INT)
                || configType.equals(TypeName.BYTE)
                || configType.equals(TypeName.CHAR)
                || configType.equals(TypeName.DOUBLE)
                || configType.equals(TypeName.LONG)
                || configType.equals(TypeName.FLOAT)
                || configType.equals(TypeName.SHORT)) {
            return "0";
        } else if (configType.equals(TypeName.BOOLEAN)) {
            return "false";
        } else {
            throw new IllegalStateException("Internal error: Exhausted all types (should be impossible)");
        }
    }
}
