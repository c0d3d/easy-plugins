package com.example1;

import org.junit.*;
import static org.junit.Assert.*;

public class StringChangerIT {

    @Test
    public void testNameChanger() {
        SuperInterface si = StringChangerRegistry.getStringChangerByName("LowerCaser");
        assertEquals("lowercase", si.changeString("LOWERCASE"));
    }
}
