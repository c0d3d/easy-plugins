package com.nlocketz.adders.impl;

import java.util.Map;
import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("OffsetAdder")
public class Adder4 implements Adder {
    private int offset;
    
    public Adder4(Map<String, String> config) {
        offset = Integer.parseInt(config.get("offset"));
    }


    @Override
    public int add(int x, int y) {
        return y + x + offset;
    }
}

