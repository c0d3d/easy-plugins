package com.nlocketz.internal;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import java.util.ServiceLoader;

import static com.nlocketz.internal.Constants.*;

public class PluginProviderInterfaceFileGenerator extends AbstractPluginFileGenerator {

    protected PluginProviderInterfaceFileGenerator(ProcessingEnvironment procEnv,
                                                   RoundEnvironment roundEnv) {
        super(procEnv, roundEnv);
    }

    @Override
    public void generate(UserMarkerAnnotation marker, ProcessorOutputCollection into) {
        TypeName providerTypeName = marker.getServiceInterfaceTypeName();
        TypeSpec.Builder typeBuilder = TypeSpec.interfaceBuilder(marker.getServiceInterfaceProviderName())
                .addMethod(
                        Util.publicAbstractMethod(GET_NAME_METHOD_NAME, STRING_TYPE_NAME)
                                .build())
                .addMethod(
                        Util.publicAbstractMethod(CREATE_NEW_METHOD_NAME, providerTypeName)
                                .build())
                .addMethod(
                        Util.publicAbstractMethod(CREATE_NEW_WITH_CONFIG_METHOD_NAME, providerTypeName)
                                .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME)
                                .build());

        ServiceLoader<EasyPluginPlugin> pluginServiceLoader = ServiceLoader.load(EasyPluginPlugin.class);
        for (EasyPluginPlugin plugin : pluginServiceLoader) {
            for (MethodSpec methodSpec : plugin.pluginProviderInterfaceMethods(marker)) {
                typeBuilder = typeBuilder.addMethod(methodSpec);
            }
        }

        TypeSpec type = typeBuilder.build();

        into.putType(marker.getOutputPackage(elements), type);
    }
}
