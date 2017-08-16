package com.example5;

public class EnclosingClass {
    @StringReturner("TheStringReturner")
    static class NestedService implements StringReturnerInterface {

        @Override
        public String getString() {
            return "theString";
        }
    }
}
