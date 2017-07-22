package com.nlocketz.internal;

public interface PluginFileGenerator {
    void generate(UserMarkerAnnotation marker,
                  ProcessorOutputCollection into);
}
