package com.nlocketz.adders.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("InterestingAdder")
public class Adder3 implements Adder {
    @Override
    public int add(int x, int y) {
        return y - (-x);
    }
}
