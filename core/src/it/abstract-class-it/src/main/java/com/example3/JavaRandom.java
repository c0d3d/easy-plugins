package com.example3;
import java.util.Random;

@RandomGen("JAVA")
public class JavaRandom extends RandomGenerator {
    @Override
    int generate(int input) {
        return new Random(input).nextInt();
    }
}
