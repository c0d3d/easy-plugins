package com.nlocketz.internal;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.List;

public interface ServiceFileBuilder {
    List<JavaFile> buildFiles(ServiceAnnotation annotation, RoundEnvironment roundEnv, ProcessingEnvironment procEnv);
}
