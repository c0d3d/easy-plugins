package com.example7;

import com.example6.Calculator;
import com.example6.CalculatorService;

@CalculatorService("Multiplier")
public class Multiplier implements Calculator {
    @Override
    public int calc(int one, int two) {
        return one * two;
    }
}
