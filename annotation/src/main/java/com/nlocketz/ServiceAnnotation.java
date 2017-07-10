package com.nlocketz;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;

class ServiceAnnotation {
    private final TypeElement annotationMarkingSP;
    private final TypeElement serviceInterface;
    private final String serviceName;
    private final String serviceNameFromAnnotaion;
    private final String outputPackage;

    ServiceAnnotation(Element annotationElement, ProcessingEnvironment procEnv) {

        // Annotations are considered interfaces, as per the documentation.
        if (!annotationElement.getKind().isInterface()) {
            throw new IllegalStateException(
                    "Annotated something that wasn't an annotation? "+annotationElement.toString());
        }

        this.annotationMarkingSP = (TypeElement) annotationElement;


        Service[] serviceAnnotations = annotationElement.getAnnotationsByType(Service.class);

        if (serviceAnnotations.length != 1) {
            throw new EZServiceException(
                    "Can only have one service annotaion, 0, or more than one detected: " + serviceAnnotations.length);
        }



        serviceInterface = procEnv.getElementUtils().getTypeElement(serviceAnnotations[0].serviceInterface());

        if (serviceInterface == null) {
            throw new EZServiceException(
                    "Couldn't find interface class names: " + serviceAnnotations[0].serviceInterface());
        }

        serviceName = serviceAnnotations[0].value();
        serviceNameFromAnnotaion = serviceAnnotations[0].annotationFieldNameForServiceName();
        outputPackage = serviceAnnotations[0].outputPackage();
    }

    /**
     * Collects all of the classes that are marked with our marker annotation, {@link #annotationMarkingSP}.
     * Does all the checks necessary to ensure they conform to our requirements of:
     * <ol>
     *     <li>Must annotate a class</li>
     *     <li>Annotated classes must be concrete</li>
     *     <li>Annotated classes must implement the interface specified in the annotation
     *     that annotated the marker annotation.
     *     </li>
     * </ol>
     * @param roundEnv The current round environment
     * @param procEnv The current processing environment
     * @return The set of classes that are marked, and have also follow our rules.
     * @throws EZServiceException if we found a non-conforming class. Message indicates the problem, as per the comment
     * on {@link EZServiceException}
     */
    Set<MarkedServiceClass> getMarkedClasses(RoundEnvironment roundEnv, ProcessingEnvironment procEnv) {

        Set <? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationMarkingSP);
        if (elements == null) {
            procEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "No annotated classes found annotated with " + annotationMarkingSP.toString());
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
            List<? extends TypeMirror> implemented = element.getInterfaces();

            if (!containsServiceInterface(implemented, procEnv)) {
                throw new EZServiceException("Classes marked with "
                        + annotationMarkingSP.getQualifiedName() + " must implement " + serviceInterface.toString());
            }

            // Now we need to collect the actual name from the annotation annotating the class
            List<? extends AnnotationMirror> annoMirrors = procEnv.getElementUtils().getAllAnnotationMirrors(element);

            AnnotationMirror selected = null;

            for (AnnotationMirror m : annoMirrors) {
                if (procEnv.getTypeUtils().isSameType(annotationMarkingSP.asType(), m.getAnnotationType().asElement().asType())) {
                    selected = m;
                }
            }

            if (selected == null) {
                throw new IllegalStateException("How did this happen?");
            }

            AnnotationValue nameValue = getValueByName(selected, serviceNameFromAnnotaion);

            if (nameValue == null) {
                throw new EZServiceException(
                        "Service annotation, "
                                + serviceName + " does not have the correct name field: " + serviceNameFromAnnotaion);
            }

            result.add(new MarkedServiceClass(procEnv, element, nameValue.toString()));
        }

        return result;
    }

    private AnnotationValue getValueByName(AnnotationMirror mirror, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                mirror.getElementValues().entrySet()) {
            ExecutableElement exec = entry.getKey();
            if (exec.getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }


    private boolean containsServiceInterface(List<? extends TypeMirror> mirrors, ProcessingEnvironment procEnv) {
        Types types = procEnv.getTypeUtils();
        TypeMirror requiredInterfaceMirror = serviceInterface.asType();
        for (TypeMirror mirror : mirrors) {
            // We currently ignore generics (hence erasure)
            // TODO figure out some way to support specific type parameters only
            if (types.isSameType(types.erasure(mirror), requiredInterfaceMirror)){
                return true;
            }
        }
        return false;
    }

    String getServiceInterfaceName() {
        return serviceName+"Provider";
    }

    String getServiceRegistryName() {
        return serviceName+"Registry";
    }

    TypeName getProviderReturnTypeName() {
        return TypeName.get(serviceInterface.asType());
    }

    String getServiceName() {
        return serviceName;
    }
    String getOutputPackage() {
        return outputPackage;
    }
}
