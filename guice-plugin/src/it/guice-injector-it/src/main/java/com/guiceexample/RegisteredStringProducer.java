package com.guiceexample;

import com.nlocketz.Service;

@Service(value = "StringProducer",
        serviceInterface = StringProducer.class,
        outputPackage = "com.guiceexample")
public @interface RegisteredStringProducer {
    String value();
}
