package com.nlocketz;


import com.google.auto.service.AutoService;
import com.nlocketz.internal.EZServiceException;
import com.nlocketz.internal.EntireServiceFileBuilder;
import com.nlocketz.internal.ServiceAnnotation;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;


@SupportedAnnotationTypes("com.nlocketz.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class EasyServiceProcessor extends AbstractProcessor {


    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        try {
            for (TypeElement ele : annotations) {
                for (Element newService : roundEnvironment.getElementsAnnotatedWith(ele)) {
                    processService(newService, roundEnvironment);
                }
            }
        } catch (EZServiceException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void processService(Element annotationElement, RoundEnvironment roundEnv) {
        ServiceAnnotation newService = new ServiceAnnotation(annotationElement, processingEnv);
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, "Making services for " + newService.getServiceInterfaceName());

        EntireServiceFileBuilder builder = new EntireServiceFileBuilder();
        List<JavaFile> allFiles = builder.buildFiles(newService, roundEnv, processingEnv);
        for (JavaFile file : allFiles) {
            writeFile(file);
        }
    }

    private void writeFile(JavaFile file) {
        try {
            file.writeTo(processingEnv.getFiler());
        } catch (FilerException e) {
            throw new EZServiceException("Couldn't create file: "+e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
