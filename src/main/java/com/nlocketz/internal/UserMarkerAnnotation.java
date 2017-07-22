package com.nlocketz.internal;

import com.nlocketz.EZServiceException;
import com.nlocketz.Service;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class UserMarkerAnnotation {
    private TypeElement markingAnnotation;
    private TypeElement serviceParentType;
    private String serviceBaseName;
    private String individualNameKey;
    private String outputPackage;

    /**
     * This constructor is used by the user's generated specialized processor.
     * The info that would normally be given by the use of {@link Service} is provided in
     * {@link MarkerAnnotation}.
     * @param userAnnotationClass The annotation that will annotate user created services.
     * @param info Configuration information.
     * @param procEnv The current processing environment.
     */
    UserMarkerAnnotation(TypeElement userAnnotationClass, MarkerAnnotation info, ProcessingEnvironment procEnv) {
        markingAnnotation = userAnnotationClass;
        serviceParentType = procEnv.getElementUtils().getTypeElement(info.getSiName());
        if (serviceParentType == null) {
            throw new IllegalStateException("User service interface class not found! " + info.getSiName());
        }
        serviceBaseName = info.getServiceName();
        individualNameKey = info.getServiceNameFromAnnotation();
        outputPackage = info.getOutputPackage();
    }

    UserMarkerAnnotation(TypeElement markingAnnotation,
                         TypeElement serviceParentType,
                         String serviceBaseName,
                         String individualNameKey,
                         String outputPackage) {
        this.markingAnnotation = markingAnnotation;
        this.serviceParentType = serviceParentType;
        this.serviceBaseName = serviceBaseName;
        this.individualNameKey = individualNameKey;
        this.outputPackage = outputPackage;
    }

    /**
     * Collects all of the classes that are marked with our marker annotation, {@link #markingAnnotation}.
     * Does all the checks necessary to ensure they conform to our requirements of:
     * <ol>
     *     <li>Must annotate a class</li>
     *     <li>Annotated classes must be concrete</li>
     *     <li>Annotated classes must implement the interface specified in the annotation
     *     that annotated the marker annotation.
     *     </li>
     * </ol>
     * @param roundEnv The current round environment
     * @param types The type utilities instance
     * @param eleUtils The element utilities instance
     * @return The set of classes that are marked, and have also follow our rules.
     * @throws EZServiceException if we found a non-conforming class. Message indicates the problem, as per the comment
     * on {@link EZServiceException}
     */
    Set<MarkedServiceClass> getMarkedClasses(RoundEnvironment roundEnv, Types types, Elements eleUtils) {

        Set <? extends Element> elements = roundEnv.getElementsAnnotatedWith(markingAnnotation);

        if (elements == null) {
            return Collections.emptySet();
        }

        // To preserve ordering, this probably doesn't matter...
        Set<MarkedServiceClass> result = new LinkedHashSet<>();

        for (Element e : elements) {
            if (!e.getKind().isClass()) {
                throw new EZServiceException(
                        "Service marking annotations can only mark classes, found: " + e.toString());
            }

            if (e.getModifiers().contains(Modifier.ABSTRACT)) {
                throw new EZServiceException(
                        "You can only mark concrete classes with service marking annotations, found abstract: " + e.toString());
            }

            TypeElement element = (TypeElement) e;

            if (types.isAssignable(serviceParentType.asType(), element.asType())) {
                throw new EZServiceException("Classes marked with "
                        + markingAnnotation.getQualifiedName() + " must implement/extend " + serviceParentType.toString()
                        + ". Found: " + element.getQualifiedName());
            }

            // Now we need to collect the actual name from the annotation annotating the class
            List<? extends AnnotationMirror> annoMirrors = eleUtils.getAllAnnotationMirrors(element);

            AnnotationMirror selected =
                    PoetUtil.getMatchingMirror(markingAnnotation.asType(), annoMirrors, types);

            if (selected == null) {
                throw new IllegalStateException("How did this happen?");
            }

            AnnotationValue nameValue = PoetUtil.getValueByName(selected, individualNameKey);

            if (nameValue == null) {
                throw new EZServiceException(
                        "Service annotation, "
                                + serviceBaseName + " does not have the correct name field: " + individualNameKey);
            }

            result.add(new MarkedServiceClass(element, nameValue.toString(), types, eleUtils));
        }

        return result;
    }

    public TypeMirror getMarkerAnnotationType() {
        return markingAnnotation.asType();
    }

    public String getServiceInterfaceName() {
        return serviceBaseName;
    }

    public String getOutputPackage() {
        return outputPackage;
    }

    public TypeMirror getServiceInterfaceType() {
        return serviceParentType.asType();
    }

    public TypeName getServiceInterfaceTypeName() {

        int paramCount = serviceParentType.getTypeParameters().size();
        if (paramCount > 0) {
            TypeName[] params = new TypeName[paramCount];
            for (int i = 0; i < paramCount; i++) {
                params[i] = PoetUtil.wildcardType();
            }

            return ParameterizedTypeName.get(ClassName.get(serviceParentType), params);
        } else {
            return TypeName.get(serviceParentType.asType());
        }
    }

    public String getIndividualNameKey() {
        return individualNameKey;
    }

    public String getRegistryServiceName() {
        return serviceBaseName + "Registry";
    }

    public String getProcessorServiceName() {
        return serviceBaseName + "Processor";
    }

    public String getServiceInterfaceProviderName() {
        return serviceBaseName + "Provider";
    }
}
