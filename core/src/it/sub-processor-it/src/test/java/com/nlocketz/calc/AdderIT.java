package com.nlocketz.calc;

import com.nlocketz.adders.api.AdderService;
import com.nlocketz.composers.api.Composer;
import org.junit.*;
import static org.junit.Assert.*;
import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.generated.AdderServiceRegistry;
import com.nlocketz.composers.generated.ComposerServiceRegistry;


public class AdderIT {

    @Test
    public void testAdder1() {
        Adder accurate = AdderServiceRegistry.getAdderServiceByName("AccurateAdder");
        assertEquals(3, accurate.add(1, 2));
    }

    @Test
    public void testAdder2() {
        Adder inaccurate = AdderServiceRegistry.getAdderServiceByName("InaccurateAdder");
        assertEquals(-1, inaccurate.add(1, 2));
    }

    @Test
    public void testAdder3() {
        Adder interesting = AdderServiceRegistry.getAdderServiceByName("InterestingAdder");
        assertEquals(0, interesting.add(1, -1));
    }

    @Test
    public void testComposer1() {
        Adder inaccurate = AdderServiceRegistry.getAdderServiceByName("InaccurateAdder");
        Adder accurate = AdderServiceRegistry.getAdderServiceByName("AccurateAdder");

        Composer multiplier = ComposerServiceRegistry.getComposerServiceByName("MultiplierComposer");
        Adder created = multiplier.compose(accurate, inaccurate);
        assertEquals(-12, created.add(2, 4));

    }

    @Test
    public void testComposer2() {
        Adder interesting = AdderServiceRegistry.getAdderServiceByName("InterestingAdder");
        Adder accurate = AdderServiceRegistry.getAdderServiceByName("AccurateAdder");

        Composer ander = ComposerServiceRegistry.getComposerServiceByName("AnderComposer");
        Adder composed = ander.compose(accurate, interesting);
        assertEquals(0xFF, composed.add(0xF, 0xF0));
    }

}
