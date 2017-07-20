package com.nlocketz.internal;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.List;

/**
 * Builds one or more {@link JavaFile}s for a {@link ServiceAnnotation}
 */
abstract class AbstractServiceFileBuilder {

    protected CompleteServiceBuilder overallBuilder;

    AbstractServiceFileBuilder(CompleteServiceBuilder overallBuilder) {
        this.overallBuilder = overallBuilder;
    }


    /**
     * Generates new java files to be used as part of the new service given by {@code annotation}.
     * @param annotation The new service annotation
     * @param roundEnv The current round environment
     * @param procEnv The current processing environment
     * @return The newly built java files
     */
    abstract List<JavaFile> buildFiles(ServiceAnnotation annotation,
                              RoundEnvironment roundEnv,
                              ProcessingEnvironment procEnv);
}
