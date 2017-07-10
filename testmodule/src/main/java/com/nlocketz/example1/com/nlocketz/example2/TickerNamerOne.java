package com.nlocketz.example1.com.nlocketz.example2;

import java.util.Map;

@TickerNamer("ABC")
public class TickerNamerOne implements NamerInterface {

    public TickerNamerOne(Map<String, String> config) {

    }

    @Override
    public String getName() {
        return "One";
    }
}
