package com.example2;

import org.junit.*;
import static org.junit.Assert.*;

public class BooleanCheckerIT {

    @Test
    public void testNotChecker() {
        Checker<?> checker = ObjectCheckerRegistry.getObjectCheckerByName("NOT");
        BooleanChecker bc = (BooleanChecker) checker;
        assertFalse(bc.check(true));

    }
}
