package com.example6;

@CalculatorService("Adder")
public class Adder implements Calculator {
    @Override
    public int calc(int one, int two) {
        return one + two;
    }
}
