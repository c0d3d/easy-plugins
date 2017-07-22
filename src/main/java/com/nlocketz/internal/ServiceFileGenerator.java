package com.nlocketz.internal;

public interface ServiceFileGenerator {
    void generate(UserMarkerAnnotation marker,
                  ProcessorOutputCollection into);
}
