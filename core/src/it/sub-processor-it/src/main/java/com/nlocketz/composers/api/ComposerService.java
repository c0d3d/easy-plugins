package com.nlocketz.composers.api;


import com.nlocketz.Service;

@Service(value = "ComposerService",
        serviceInterface = Composer.class,
        outputPackage = "com.nlocketz.composers.generated",
        serviceNameKey = "composerName")
public @interface ComposerService {
    String composerName();
}
