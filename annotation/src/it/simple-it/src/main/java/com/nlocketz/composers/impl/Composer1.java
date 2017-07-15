package com.nlocketz.composers.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.composers.api.Composer;
import com.nlocketz.composers.api.ComposerService;

@ComposerService(composerName = "MultiplierComposer")
public class Composer1 implements Composer {
    @Override
    public Adder compose(final Adder one, Adder two) {
        return (x, y) -> one.add(x, y) * two.add(x, y);
    }
}
