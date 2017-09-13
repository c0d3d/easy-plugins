package com.nlocketz.adders.impl;

import java.util.Map;
import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("TypedOffsetAdder")
public class Adder5 implements Adder {
    private int offset;
    
    public Adder5(int offset) {
        this.offset = offset;
    }


    @Override
    public int add(int x, int y) {
        return y + x + offset;
    }
}

