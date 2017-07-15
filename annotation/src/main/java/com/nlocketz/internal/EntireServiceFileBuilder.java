package com.nlocketz.internal;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EntireServiceFileBuilder implements ServiceFileBuilder {

    private static final List<ServiceFileBuilder> subcomponentBuilders = Arrays.asList(
            new ServiceProviderInterfaceFileBuilder(),
            new ServiceRegistryFileBuilder(),
            new ServiceProviderFileBuilder()
    );

    @Override
    public List<JavaFile> buildFiles(ServiceAnnotation annotation, RoundEnvironment roundEnv, ProcessingEnvironment procEnv) {
        List<JavaFile> results = new LinkedList<>();
        for (ServiceFileBuilder subBuilder : subcomponentBuilders) {
            results.addAll(subBuilder.buildFiles(annotation, roundEnv, procEnv));
        }
        return results;
    }
}
