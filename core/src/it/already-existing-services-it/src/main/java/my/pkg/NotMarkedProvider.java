package my.pkg;

import java.util.Map;
import com.example3.StupidProvider;

public class NotMarkedProvider implements StupidProvider {

    public NotMarkedProvider() {
    }

    public final String getProviderName() {
        return "NOT_DUMB";
    }

    public final NotMarked create() {
        return new NotMarked();
    }

    public final NotMarked createWithConfig(Object config) {
        return new NotMarked();
    }
}