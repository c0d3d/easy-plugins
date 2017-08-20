package com.guiceexample;

@RegisteredStringProducer("UNINJECTED")
public class UninjectedStringProducer implements StringProducer {
    @Override
    public String getString() {
        return "Hello, World";
    }
}
