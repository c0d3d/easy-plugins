package com.nlocketz.adders.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;
import com.nlocketz.ConfigurationConstructor;

@AdderService("TypedOffsetAdderMulti")
public class Adder6 implements Adder {
    private int offset;

    @ConfigurationConstructor
    public Adder6(int offset) {
        this.offset = offset;
    }

    public Adder6(boolean notAConfig) {
        throw new RuntimeException("This should not run.");
    }


    @Override
    public int add(int x, int y) {
        return y + x + offset;
    }
}

