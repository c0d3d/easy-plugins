package com.nlocketz.internal;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

public final class CompleteServiceGenerator {

    /**
     * Builds a new pipeline of {@link ServiceFileGenerator}s.
     * @param procEnv The current processing environment.
     * @param roundEnv The current round environment.
     * @return The newly built pipeline.
     */
    private static List<ServiceFileGenerator> generatorList(ProcessingEnvironment procEnv,
                                                            RoundEnvironment roundEnv) {
        return Arrays.asList(
                new ServiceProviderInterfaceFileGenerator(procEnv, roundEnv),
                new ServiceProviderFileGenerator(procEnv, roundEnv),
                new ServiceRegistryFileGenerator(procEnv, roundEnv)//,
                //new ServiceAnnotationProcessorGenerator(procEnv, roundEnv)
        );
    }

    private CompleteServiceGenerator() {

    }

    private static ProcessorOutputCollection buildFiles(
            UserMarkerAnnotation annotation,
            ProcessingEnvironment procEnv,
            RoundEnvironment roundEnv,
            ProcessorOutputCollection output) {

        for (ServiceFileGenerator subGen : generatorList(procEnv, roundEnv)) {
            subGen.generate(annotation, output);
        }

        return output;
    }

    private static void buildServiceFilesInternal(UserMarkerAnnotation annotation,
                                                  RoundEnvironment roundEnv,
                                                  ProcessingEnvironment procEnv,
                                                  ProcessorOutputCollection output) {

        buildFiles(annotation, procEnv, roundEnv, output);
    }

    public static void buildServiceFiles(Element annotationElement,
                                                   RoundEnvironment roundEnv,
                                                   ProcessingEnvironment procEnv,
                                                   ProcessorOutputCollection output) {
        buildServiceFilesInternal(
                ServiceAnnotation.createUserMarker(annotationElement, procEnv), roundEnv, procEnv, output);


    }

    public static void buildSpecializedServiceFiles(Element annotationElement,
                                                              MarkerAnnotation ma,
                                                              RoundEnvironment roundEnv,
                                                              ProcessingEnvironment procEnv,
                                                              ProcessorOutputCollection output) {
        if (!annotationElement.getKind().isInterface()) {
            throw new IllegalStateException("Specialized processor must be given annotation");
        }

        buildServiceFilesInternal(
                new UserMarkerAnnotation((TypeElement)annotationElement, ma, procEnv),
                roundEnv,
                procEnv,
                output);
    }
}
