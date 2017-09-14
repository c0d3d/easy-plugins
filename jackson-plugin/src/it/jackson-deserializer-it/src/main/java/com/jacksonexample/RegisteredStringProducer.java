package com.jacksonexample;

import com.nlocketz.Service;

@Service(value = "StringProducer",
        serviceInterface = StringProducer.class,
        outputPackage = "com.jacksonexample")
public @interface RegisteredStringProducer {
    String value();
}