package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.nlocketz.internal.GeneratedNameConstants.*;
import static com.nlocketz.internal.PoetUtil.publicFinalMethod;

/**
 * Builds the service providers for our internal service
 * (the one created by {@link ServiceProviderInterfaceFileBuilder}).
 * There will be one provider for every {@link MarkedServiceClass} within a given {@link ServiceAnnotation}.
 */
public class ServiceProviderFileBuilder extends AbstractServiceFileBuilder {

    ServiceProviderFileBuilder(CompleteServiceBuilder overallBuilder) {
        super(overallBuilder);
    }

    @Override
    public List<JavaFile> buildFiles(ServiceAnnotation annotation, RoundEnvironment roundEnv, ProcessingEnvironment procEnv) {
        List<JavaFile> result = new LinkedList<>();
        List<String> processorQNames = new LinkedList<>();
        for (MarkedServiceClass marked : annotation.getMarkedClasses(roundEnv, procEnv)) {
            result.add(buildSingleMarkedClass(
                    marked,
                    annotation.getServiceInterfaceName(),
                    annotation.getOutputPackage(),
                    processorQNames));
        }
        String spInterface = annotation.getOutputPackage() + "." + annotation.getServiceInterfaceName();
        overallBuilder.addToSpiOutput(spInterface, new HashSet<>(processorQNames));
        return result;
    }



    private JavaFile buildSingleMarkedClass(MarkedServiceClass marked,
                                            String interfaceName,
                                            String outputPkg,
                                            List<String> processorQNames) {

        String className = marked.getNewServiceClassName();
        processorQNames.add(outputPkg + "." + className);

        MethodSpec getName = publicFinalMethod(GET_NAME_METHOD_NAME, STRING_TYPE_NAME)
                .addStatement("return $L", marked.getServiceName())
                .build();

        MethodSpec.Builder createBuilder = publicFinalMethod(CREATE_NEW_METHOD_NAME, marked.getTypeName());
        marked.addDefaultConstructorCall(createBuilder);

        MethodSpec.Builder createWithConfigBuilder =
                publicFinalMethod(CREATE_NEW_WITH_CONFIG_METHOD_NAME, marked.getTypeName())
                        .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME);
        marked.addMapConstructorCall(createWithConfigBuilder, CONFIG_ARG_NAME);

        TypeSpec clazz = TypeSpec.classBuilder(className)
                .addSuperinterface(ClassName.get(outputPkg, interfaceName))
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(getName)
                .addMethod(createBuilder.build())
                .addMethod(createWithConfigBuilder.build())
                .build();

        return JavaFile.builder(outputPkg, clazz).build();
    }
}
