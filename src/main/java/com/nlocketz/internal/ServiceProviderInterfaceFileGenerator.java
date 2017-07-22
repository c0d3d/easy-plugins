package com.nlocketz.internal;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.Collections;
import java.util.List;

import static com.nlocketz.internal.GeneratedNameConstants.*;

public class ServiceProviderInterfaceFileGenerator extends AbstractServiceFileGenerator {

    protected ServiceProviderInterfaceFileGenerator(ProcessingEnvironment procEnv,
                                                    RoundEnvironment roundEnv) {
        super(procEnv, roundEnv);
    }

    @Override
    public void generate(UserMarkerAnnotation marker, ProcessorOutputCollection into) {
        TypeName providerTypeName = marker.getServiceInterfaceTypeName();
        TypeSpec type = TypeSpec.interfaceBuilder(marker.getServiceInterfaceProviderName())
                .addMethod(
                        PoetUtil.publicAbstractMethod(GET_NAME_METHOD_NAME, STRING_TYPE_NAME)
                                .build())
                .addMethod(
                        PoetUtil.publicAbstractMethod(CREATE_NEW_METHOD_NAME, providerTypeName)
                                .build())
                .addMethod(
                        PoetUtil.publicAbstractMethod(CREATE_NEW_WITH_CONFIG_METHOD_NAME, providerTypeName)
                                .addParameter(MAP_STRING_STRING_NAME, CONFIG_ARG_NAME)
                                .build())
                .build();

        into.putType(marker.getOutputPackage(), type);
    }
}
