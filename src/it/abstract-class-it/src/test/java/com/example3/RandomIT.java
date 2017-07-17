package com.example3;

import org.junit.*;

public class RandomIT {
    @Test
    public void testRandom() {
        RandomGenerator r = RandomGeneratorRegistry.getRandomGeneratorByName("JAVA");
    }
}
