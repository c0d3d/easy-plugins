package com.nlocketz.example1.com.nlocketz.example2;

import com.nlocketz.Service;

@Service(value = "TickerNamer",
        serviceInterface = NamerInterface.class,
        outputPackage = "pkg.packag")
public @interface TickerNamer {
    String value();
}
