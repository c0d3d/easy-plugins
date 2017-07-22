package com.example5;

import com.nlocketz.Service;

@Service(value = "StringReturner",
        serviceInterface = StringReturnerInterface.class,
        outputPackage = "com.example5")
public @interface StringReturner {
    String value();
}
