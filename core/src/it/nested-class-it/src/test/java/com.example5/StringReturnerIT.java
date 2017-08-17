package com.example5;


import org.junit.*;
import static org.junit.Assert.*;


public class StringReturnerIT {
    @Test
    public void testStringReturner() {
        StringReturnerInterface i = StringReturnerRegistry.getStringReturnerByName("TheStringReturner");
        assertEquals("theString", i.getString());
    }
}
