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
import javax.tools.Diagnostic;
import java.util.*;

/**
 * Utility class to extract information about a new marked user service.
 */
class ServiceAnnotation {
    private ServiceAnnotation() {

    }

    /**
     * Extracts the {@link UserMarkerAnnotation} from the given element, which is expected to be an annotation
     * annotated with {@link Service}. (This is checked).
     * @param annotationElement The marked annotation.
     * @param procEnv The current processing environment.
     * @return The extracted UserMarkerAnnotation
     */
    static UserMarkerAnnotation createUserMarker(Element annotationElement, ProcessingEnvironment procEnv) {

        Elements elements = procEnv.getElementUtils();

        // Annotations are considered interfaces, as per the documentation.
        if (!annotationElement.getKind().isInterface()) {
            throw new IllegalStateException(
                    "Annotated something that wasn't an annotation? "+annotationElement.toString());
        }

        TypeElement annotationMarkingSP = (TypeElement) annotationElement;


        Service[] serviceAnnotations = annotationElement.getAnnotationsByType(Service.class);

        if (serviceAnnotations.length != 1) {
            throw new EZServiceException(
                    "Can only have one service annotaion, 0, or more than one detected: " + serviceAnnotations.length);
        }

        AnnotationMirror serviceMirror =
                PoetUtil.getMatchingMirror(
                        elements.getTypeElement(Service.class.getName()).asType(),
                        elements.getAllAnnotationMirrors(annotationElement),
                        procEnv.getTypeUtils());

        // We can't get the interface the normal way (via the annotation's methods)
        // Since Class<?>'s are a runtime construct and require classloading ... etc
        String interfaceQName = PoetUtil.getValueStringByName(serviceMirror, "serviceInterface");

        TypeElement serviceClass = elements.getTypeElement(interfaceQName);
        if (serviceClass == null) {
            throw new EZServiceException(
                    "Couldn't find interface class named: " + interfaceQName);
        }

        String serviceName = serviceAnnotations[0].value();
        String serviceNameFromAnnotaion = serviceAnnotations[0].annotationFieldNameForServiceName();
        String outputPackage = serviceAnnotations[0].outputPackage();

        return new UserMarkerAnnotation(
                annotationMarkingSP,
                serviceClass,
                serviceName,
                serviceNameFromAnnotaion,
                outputPackage);
    }
}
