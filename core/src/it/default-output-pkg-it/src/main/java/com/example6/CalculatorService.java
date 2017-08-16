package com.example6;

import com.nlocketz.Service;

@Service(value = "Calculator",
        serviceInterface = Calculator.class)
public @interface CalculatorService {
    String value();
}
