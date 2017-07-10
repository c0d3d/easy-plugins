package com.nlocketz.example1.com.nlocketz.example2;

import com.nlocketz.Service;

@Service(value = "TickerNamer",
        serviceInterface = "com.nlocketz.example1.com.nlocketz.example2.NamerInterface",
        outputPackage = "pkg.packag")
public @interface TickerNamer {
    String value();
}
