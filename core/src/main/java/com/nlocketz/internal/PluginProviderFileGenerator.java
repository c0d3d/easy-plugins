package com.nlocketz.internal;

import com.squareup.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collections;

import static com.nlocketz.internal.Constants.*;
import static com.nlocketz.internal.Util.publicFinalMethod;

/**
 * Builds the service providers for our internal service
 * (the one created by {@link PluginProviderInterfaceFileGenerator}). //TODO
 * There will be one provider for every {@link MarkedPluginClass} within a given {@link PluginAnnotation}.
 */
class PluginProviderFileGenerator extends AbstractPluginFileGenerator {

    protected PluginProviderFileGenerator(ProcessingEnvironment procEnv, RoundEnvironment roundEnv) {
        super(procEnv, roundEnv);
    }

    private void buildSingleMarkedClass(MarkedPluginClass marked,
                                        String interfaceName,
                                        String outputPkg,
                                        ProcessorOutputCollection into) {
        if (outputPkg.equals("")) {
            outputPkg = elements.getPackageOf(marked.getAnnotatedEle()).toString();
        }

        String className = marked.getNewServiceClassName();
        ClassName serviceInterfaceName = ClassName.get(outputPkg, interfaceName);

        MethodSpec getName = publicFinalMethod(GET_NAME_METHOD_NAME, STRING_TYPE_NAME)
                .addStatement("return $L", marked.getServiceName())
                .build();

        MethodSpec.Builder createBuilder = publicFinalMethod(CREATE_NEW_METHOD_NAME, marked.getTypeName());
        marked.addDefaultConstructorCall(createBuilder);

        MethodSpec.Builder createWithConfigBuilder =
                publicFinalMethod(CREATE_NEW_WITH_CONFIG_METHOD_NAME, marked.getTypeName())
                        .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME);
        marked.addMapConstructorCall(createWithConfigBuilder, CONFIG_ARG_NAME);

        TypeSpec.Builder clazzBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(serviceInterfaceName)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(getName)
                .addMethod(createBuilder.build())
                .addMethod(createWithConfigBuilder.build());

        for (EasyPluginPlugin plugin : Util.getPluginLoader()) {
            for (MethodSpec methodSpec : plugin.pluginProviderMethods(marked)) {
               clazzBuilder = clazzBuilder.addMethod(methodSpec);
            }
        }

        TypeSpec clazz = clazzBuilder.build();

        TypeElement implementorTypeElement = elements.getTypeElement(marked.getTypeName().toString());

        // Check for visibility issues, and a non concrete type ...
        Util.checkElementVisibility(elements, implementorTypeElement, outputPkg);
        Util.checkConcreteType(implementorTypeElement);

        into.putType(outputPkg, clazz, Collections.singletonList(serviceInterfaceName));
    }

    @Override
    public void generate(UserMarkerAnnotation marker, ProcessorOutputCollection into) {
        for (MarkedPluginClass marked : marker.getMarkedClasses(roundEnv, types, elements)) {
            buildSingleMarkedClass(
                    marked,
                    marker.getServiceInterfaceProviderName(),
                    marker.getOutputPackage(elements),
                    into);
        }
    }
}
