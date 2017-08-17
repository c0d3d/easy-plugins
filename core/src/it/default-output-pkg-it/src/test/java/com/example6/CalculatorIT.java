package com.example6;

import org.junit.*;


public class CalculatorIT {
    @Test
    public void testCalc() {
        Calculator c1 = CalculatorRegistry.getCalculatorByName("Adder");
        Calculator c2 = CalculatorRegistry.getCalculatorByName("Multiplier");
        Assert.assertEquals(24, c1.calc(c2.calc(3, 4), c2.calc(4, 3)));
    }
}
