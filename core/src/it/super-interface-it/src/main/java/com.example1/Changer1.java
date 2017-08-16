package com.example1;

@NameChanger("LowerCaser")
public class Changer1 implements SubInterface {

    public String changeString(String s) {
        return s.toLowerCase();
    }
}
