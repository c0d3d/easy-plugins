package com.jacksonexample;

@RegisteredStringProducer("SIMPLE")
class SimpleStringProducer implements StringProducer {
    @Override
    public String getString() {
        return "simple";
    }
}