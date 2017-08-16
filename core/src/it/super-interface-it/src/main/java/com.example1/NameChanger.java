package com.example1;

import com.nlocketz.Service;

@Service(value = "StringChanger",
        serviceInterface = SuperInterface.class,
        outputPackage = "com.example1")
public @interface NameChanger {
    String value();
}
