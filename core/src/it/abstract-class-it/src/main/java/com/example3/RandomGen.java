package com.example3;

import com.nlocketz.Service;

@Service(value = "RandomGenerator",
        serviceInterface = RandomGenerator.class,
        outputPackage = "com.example3")
public @interface RandomGen {
    String value();
}
