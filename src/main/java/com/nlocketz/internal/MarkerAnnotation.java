package com.nlocketz.internal;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;

import static com.nlocketz.internal.Constants.MARKER_ANNOTATION_CLASS_NAME;

class MarkerAnnotation {
    private String siName;
    private String serviceName;
    private String serviceNameFromAnnotation;
    private String outputPackage;

    public MarkerAnnotation(String siName, String serviceName, String serviceNameFromAnnotation, String outputPackage) {
        this.siName = siName;
        this.serviceName = serviceName;
        this.serviceNameFromAnnotation = serviceNameFromAnnotation;
        this.outputPackage = outputPackage;
    }

    public String getSiName() {
        return siName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceNameFromAnnotation() {
        return serviceNameFromAnnotation;
    }

    public String getOutputPackage() {
        return outputPackage;
    }

    /**
     * Adds a statement creating a new MarkerAnnotation instance to builder.
     * Returns the name of the instance.
     * @param builder The builder to add to.
     * @param annotation The service annotation to gather info from.
     * @return The name of the new instance.
     */
    public static String addMarkerAnnotationInstance(MethodSpec.Builder builder, UserMarkerAnnotation annotation, Types types) {

        String siName =
                ((TypeElement)types.asElement(annotation.getServiceInterfaceType())).getQualifiedName().toString();
        builder.addComment("The following values were gathered from the original @Service annotation parameters,");
        builder.addComment("and included here for this specialized service processor.");
        builder.addStatement("$T marker = new $T($S, $S, $S, $S)",
                MARKER_ANNOTATION_CLASS_NAME,
                MARKER_ANNOTATION_CLASS_NAME,
                siName,
                annotation.getServiceInterfaceName(),
                annotation.getIndividualNameKey(),
                annotation.getOutputPackage());

        return "marker";
    }
}
