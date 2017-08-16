package com.nlocketz.adders.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("AccurateAdder")
public class Adder1 implements Adder {
    public int add(int x, int y) {
        return x+y;
    }
}
