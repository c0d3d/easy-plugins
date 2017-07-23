package com.subservice;

import com.nlocketz.adders.api.AdderService;
import com.nlocketz.adders.api.Adder;

@AdderService("xor")
public class Subservice implements Adder {
    public int add(int x, int y) {
        return x ^ y;
    }
}
