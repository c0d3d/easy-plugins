package com.nlocketz.internal;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

public final class CompletePluginGenerator {

    /**
     * Builds a new pipeline of {@link PluginFileGenerator}s.
     * @param procEnv The current processing environment.
     * @param roundEnv The current round environment.
     * @return The newly built pipeline.
     */
    private static List<PluginFileGenerator> generatorList(ProcessingEnvironment procEnv,
                                                           RoundEnvironment roundEnv,
                                                           boolean specialized) {
        List<PluginFileGenerator> gens = new LinkedList<>();
        gens.add(new PluginProviderInterfaceFileGenerator(procEnv, roundEnv));
        gens.add(new PluginProviderFileGenerator(procEnv, roundEnv));
        gens.add(new PluginRegistryFileGenerator(procEnv, roundEnv));
        if (!specialized) {
            gens.add(new PluginAnnotationProcessorGenerator(procEnv, roundEnv));
        }
        return gens;
    }

    private CompletePluginGenerator() {

    }

    private static ProcessorOutputCollection buildFiles(
            UserMarkerAnnotation annotation,
            ProcessingEnvironment procEnv,
            RoundEnvironment roundEnv,
            ProcessorOutputCollection output,
            boolean specialized) {

        for (PluginFileGenerator subGen : generatorList(procEnv, roundEnv, specialized)) {
            subGen.generate(annotation, output);
        }

        return output;
    }

    public static void buildServiceFiles(Element annotationElement,
                                         RoundEnvironment roundEnv,
                                         ProcessingEnvironment procEnv,
                                         ProcessorOutputCollection output) {
        buildFiles(
                PluginAnnotation.createUserMarker(annotationElement, procEnv),
                procEnv,
                roundEnv,
                output,
                false);


    }

    public static void buildSpecializedServiceFiles(Element annotationElement,
                                                    MarkerAnnotation ma,
                                                    RoundEnvironment roundEnv,
                                                    ProcessingEnvironment procEnv,
                                                    ProcessorOutputCollection output) {
        if (!annotationElement.getKind().isInterface()) {
            throw new IllegalStateException("Specialized processor must be given annotation");
        }

        buildFiles(new UserMarkerAnnotation((TypeElement)annotationElement, ma, procEnv),
                procEnv,
                roundEnv,
                output,
                true);
    }
}
