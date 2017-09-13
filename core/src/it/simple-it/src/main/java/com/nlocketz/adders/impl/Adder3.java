package com.nlocketz.adders.impl;

import java.util.Map;
import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;
import com.nlocketz.ConfigurationConstructor;

@AdderService("InterestingAdder")
public class Adder3 implements Adder {

    @ConfigurationConstructor
    public Adder3() {
        // empty
    }

    public Adder3(Map<String, String> notAConfig) {
        throw new RuntimeException("This should not run");
    }

    @Override
    public int add(int x, int y) {
        return y - (-x);
    }
}
