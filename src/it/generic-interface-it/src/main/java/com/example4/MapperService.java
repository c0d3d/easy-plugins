package com.example4;

import com.nlocketz.Service;

@Service(value = "MapperService",
        serviceInterface = MapperInterface.class,
        outputPackage = "com.example4")
public @interface MapperService {
    String value();
}
