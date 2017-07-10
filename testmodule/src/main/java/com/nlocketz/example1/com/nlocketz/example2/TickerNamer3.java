package com.nlocketz.example1.com.nlocketz.example2;

import java.util.Map;

@TickerNamer("TRE")
public class TickerNamer3 implements NamerInterface {
    public TickerNamer3() {

    }

    public TickerNamer3(Map<String,String> config) {

    }

    @Override
    public String getName() {
        return "3";
    }
}
