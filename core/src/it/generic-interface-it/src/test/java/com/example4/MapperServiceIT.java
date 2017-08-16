package com.example4;

import com.example4.MapperInterface;
import org.junit.*;
import static org.junit.Assert.*;

public class MapperServiceIT {
    @Test
    public void testJunkMapper() {
        MapperInterface<?, ?, ?> instance = MapperServiceRegistry.getMapperServiceByName("junk");
    }
}
