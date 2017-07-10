package com.nlocketz;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MarkedServiceClass {

    private TypeElement clazzElement;

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

    public MarkedServiceClass(ProcessingEnvironment env,
                              TypeElement clazzElement,
                              String serviceName) {

        this.clazzElement = clazzElement;
        MutBool defaultConst = new MutBool();
        MutBool mapConst = new MutBool();
        computeConstructors(env, clazzElement, defaultConst, mapConst);

        if (!defaultConst.val && !mapConst.val) {
            throw new EZServiceException(
                    "Services must have either a default constructor, or one taking in java.util.Map<String,String>");
        }

        if (defaultConst.val) {
            defaultConstString = "return new " + clazzElement.getQualifiedName() + "()";
        }

        if (mapConst.val) {
            constWithMap = "return new " + clazzElement.getQualifiedName() + "(%s)";
        }

        this.serviceName = serviceName;

    }

    TypeName getTypeName() {
        return TypeName.get(clazzElement.asType());
    }

    private static class MutBool {
        boolean val;
    }

    /**
     * Discovers whether this marked service class has the default and map valued constructors
     * Assigns findings to {@code }
     * @param env
     * @param clazzElement
     * @param defaultConst
     * @param mapConst
     */
    private static void computeConstructors(ProcessingEnvironment env,
                                                 TypeElement clazzElement,
                                                 MutBool defaultConst,
                                                 MutBool mapConst) {

        for (Element e : clazzElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) e;
                List<? extends VariableElement> params = ee.getParameters();
                if (!defaultConst.val && params.size() == 0) {
                    defaultConst.val = true;
                }
                if (!mapConst.val && params.size() == 1) {
                    VariableElement ele = params.get(0);
                    TypeMirror varType = env.getTypeUtils().erasure(ele.asType());
                    TypeMirror m = env.getElementUtils().getTypeElement(Map.class.getCanonicalName()).asType();
                    if (env.getTypeUtils().isAssignable(m, varType)) {
                        mapConst.val = true;
                    }
                }
            }
        }
    }



    void addMapConstructorCall(MethodSpec.Builder getByNameBuilder, String mapName) {
        if (constWithMap != null) {
            getByNameBuilder.addStatement(String.format(constWithMap, "$L"), mapName);
        } else {
            // Note: This won't be null bc of invariant
            getByNameBuilder.addStatement(defaultConstString);
        }
    }

    void addDefaultConstructorCall(MethodSpec.Builder getByNameBuilder) {
        if (defaultConstString != null) {
            getByNameBuilder.addStatement(defaultConstString);
        } else {
            getByNameBuilder.addStatement(String.format(constWithMap, "$T.<String,String>emptyMap()"), Collections.class);
        }
    }

    String getServiceName() {
        return serviceName;
    }

    String getNewServiceClassName() {
        return clazzElement.getQualifiedName().toString().replace('.','$') + "$Service$" + serviceName.replace("\"", "");
    }
}
