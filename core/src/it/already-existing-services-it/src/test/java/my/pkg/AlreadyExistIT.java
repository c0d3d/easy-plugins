package my.pkg;

import org.junit.*;
import com.example3.*;

public class AlreadyExistIT {

    @Test
    public void testProvider2() {
        StupidInterface s = StupidRegistry.getStupidByName("NOT_DUMB");
        Assert.assertEquals(0, s.number());
    }

    @Test
    public void testProvider1() {
        StupidInterface s = StupidRegistry.getStupidByName("DUMB");
        Assert.assertEquals(1, s.number());
    }
}