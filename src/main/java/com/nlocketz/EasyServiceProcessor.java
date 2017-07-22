package com.nlocketz;


import com.google.auto.service.AutoService;
import com.nlocketz.internal.CompleteServiceGenerator;
import com.nlocketz.internal.ProcessorOutputCollection;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;


@SupportedAnnotationTypes("com.nlocketz.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class EasyServiceProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        ProcessorOutputCollection output = ProcessorOutputCollection.empty();

        try {
            for (TypeElement ele : annotations) {
                // For each annotated annotation we build and generate new source files.
                for (Element newService : roundEnvironment.getElementsAnnotatedWith(ele)) {
                    processService(newService, roundEnvironment, output);
                }
            }
        } catch (EZServiceException e) {
            // See comment on EZServiceException
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } catch (Exception e) {
            // Everything else is a problem ...
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        output.writeContents(processingEnv.getFiler());

        // We are done here ...
        return false;
    }

    /**
     * Generates all the source files for the marked {@link Element}.
     * The marked element should correspond to a user written annotation annotated with {@link Service}.
     * @param annotationElement The marked element.
     * @param roundEnv The current round environment.
     */
    private void processService(Element annotationElement,
                                RoundEnvironment roundEnv,
                                ProcessorOutputCollection output) {
        CompleteServiceGenerator.buildServiceFiles(annotationElement, roundEnv, processingEnv, output);
    }
}
