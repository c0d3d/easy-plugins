package com.example2;

import com.nlocketz.Service;

@Service(value = "ObjectChecker",
        serviceInterface = Checker.class,
        outputPackage = "com.example2")
public @interface CheckerService {
    String value();
}
