package com.nlocketz.adders.api;

import com.nlocketz.Service;

@Service(value = "AdderService",
        serviceInterface = Adder.class,
        outputPackage = "com.nlocketz.adders.generated")
public @interface AdderService {
    String value();
}
