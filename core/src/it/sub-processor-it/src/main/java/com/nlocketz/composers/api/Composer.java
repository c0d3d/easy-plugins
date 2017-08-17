package com.nlocketz.composers.api;

import com.nlocketz.Service;
import com.nlocketz.adders.api.Adder;

public interface Composer {
    Adder compose(Adder one, Adder two);
}
