package com.nlocketz.adders.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("InaccurateAdder")
public class Adder2 implements Adder {
    @Override
    public int add(int x, int y) {
        return x - y;
    }
}
