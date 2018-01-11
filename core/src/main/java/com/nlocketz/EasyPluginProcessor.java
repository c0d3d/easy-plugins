package com.nlocketz;


import com.google.auto.service.AutoService;
import com.nlocketz.internal.CompletePluginGenerator;
import com.nlocketz.internal.Constants;
import com.nlocketz.internal.EasyPluginException;
import com.nlocketz.internal.ProcessorOutputCollection;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;


@SupportedAnnotationTypes("com.nlocketz.Service")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public final class EasyPluginProcessor extends AbstractProcessor {
    private static final byte[] BUFFER = new byte[0x100];

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        ProcessorOutputCollection output = ProcessorOutputCollection.empty();

        try {
            for (TypeElement ele : annotations) {
                // For each annotated annotation we build and generate new source files.
                for (Element newService : roundEnvironment.getElementsAnnotatedWith(ele)) {
                    processService(newService, roundEnvironment, output);
                }
            }
        } catch (EasyPluginException e) {
            // See comment on EasyPluginException
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        } catch (Exception e) {
            // Everything else is a problem ...
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        output.writeContents(processingEnv.getFiler());
        emitNeededClasses();

        // We are done here ...
        return false;
    }



    private void cp(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        int didRead;
        while ((didRead = in.read(BUFFER)) != -1) {
            out.write(BUFFER, 0, didRead);
        }
    }

    private void emitNeededClasses() {

        try {
            for (URL u : Constants.classesToCopy) {
                String outputName =
                        u.getPath().split("!"+File.separator)[1].replace(File.separatorChar, '.').replace(".class", "");
                try {
                    try (BufferedInputStream in = new BufferedInputStream(u.openStream())) {
                        try (BufferedOutputStream out = new BufferedOutputStream(
                                processingEnv.getFiler().createClassFile(outputName).openOutputStream())) {
                            cp(in, out);
                        }
                    }
                } catch (FilerException e) {
                    // Already existed ...
                }
            }
        } catch (IOException e) {
            throw new EasyPluginException(e.getMessage());
        }
    }

    /**
     * Generates all the source files for the marked {@link Element}.
     * The marked element should correspond to a user written annotation annotated with {@link Service}.
     *
     * @param annotationElement The marked element.
     * @param roundEnv          The current round environment.
     */
    private void processService(Element annotationElement,
                                RoundEnvironment roundEnv,
                                ProcessorOutputCollection output) {
        CompletePluginGenerator.buildServiceFiles(annotationElement, roundEnv, processingEnv, output);
    }
}
