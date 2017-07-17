package com.example2;

@CheckerService("NOT")
public class NotChecker implements BooleanChecker {

    @Override
    public boolean check(Boolean b) {
        return !b;
    }
}
