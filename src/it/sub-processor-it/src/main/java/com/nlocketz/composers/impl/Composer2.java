package com.nlocketz.composers.impl;

import com.nlocketz.adders.api.Adder;
import com.nlocketz.composers.api.Composer;
import com.nlocketz.composers.api.ComposerService;

@ComposerService(composerName = "AnderComposer")
public class Composer2 implements Composer {
    @Override
    public Adder compose(final Adder one, final Adder two) {
	return new Adder() {
	    public int add(int x, int y) {
		return one.add(x, y) & two.add(x, y);
	    }
	};
    }
}
