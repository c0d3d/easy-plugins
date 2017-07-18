package com.example3;

import org.junit.*;

public class RandomIT {
    @Test
    public void testRandom() {
        RandomGenerator r = RandomGeneratorRegistry.getRandomGeneratorByName("JAVA");
        // Seeded this happens to be the correct output.
        Assert.assertEquals(-1157408321, r.generate(5));
    }
}
