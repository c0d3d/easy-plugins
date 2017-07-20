package com.nlocketz.internal;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

public final class CompleteServiceBuilder  {

    private List<AbstractServiceFileBuilder> builderList() {
        return Arrays.asList(
                new ServiceProviderInterfaceFileBuilder(this),
                new ServiceRegistryFileBuilder(this),
                new ServiceProviderFileBuilder(this)
                //new ServiceAnnotationProcessorBuilder(this)
        );
    }

    private static Map<String, Set<String>> newServices = new HashMap<>();

    private CompleteServiceBuilder() {

    }

    private List<JavaFile> buildFiles(ServiceAnnotation annotation, RoundEnvironment roundEnv, ProcessingEnvironment procEnv) {
        List<JavaFile> results = new LinkedList<>();
        for (AbstractServiceFileBuilder subBuilder : builderList()) {
            results.addAll(subBuilder.buildFiles(annotation, roundEnv, procEnv));
        }
        return results;
    }

    /**
     * Adds a service provider registration to be written at the end of the build cycle.
     * If there already exists a mapping new providers are added.
     * @param siQName The service interface qualified names.
     * @param providerQNames Set of provider qualified names.
     */
    void addToSpiOutput(String siQName, Set<String> providerQNames) {
        if (newServices.containsKey(siQName)) {
            newServices.get(siQName).addAll(providerQNames);
        } else {
            newServices.put(siQName, new HashSet<>(providerQNames));
        }
    }

    private static List<JavaFile> buildServiceFilesInternal(ServiceAnnotation annotation,
                                                            RoundEnvironment roundEnv,
                                                            ProcessingEnvironment procEnv) {
        return new CompleteServiceBuilder().buildFiles(annotation, roundEnv, procEnv);
    }

    public static List<JavaFile> buildServiceFiles(Element annotationElement,
                                                   RoundEnvironment roundEnv,
                                                   ProcessingEnvironment procEnv) {
        return buildServiceFilesInternal(new ServiceAnnotation(annotationElement, procEnv), roundEnv, procEnv);


    }

    public static List<JavaFile> buildSpecializedServiceFiles(Element annotationElement,
                                                              MarkerAnnotation ma,
                                                              RoundEnvironment roundEnv,
                                                              ProcessingEnvironment procEnv) {
        if (!annotationElement.getKind().isInterface()) {
            throw new IllegalStateException("Specialized processor must be given annotation");
        }

        return buildServiceFilesInternal(
                new ServiceAnnotation((TypeElement)annotationElement, ma, procEnv),
                roundEnv,
                procEnv);
    }

    public static void writeSPIS(Filer filer) {
        for (String siQname : newServices.keySet()) {
            PoetUtil.writeMetaInfServices(siQname, newServices.get(siQname), filer);
        }
    }
}
