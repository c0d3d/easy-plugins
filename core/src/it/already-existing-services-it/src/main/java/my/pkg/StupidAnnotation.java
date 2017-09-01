package my.pkg;

import com.nlocketz.Service;

@Service(value = "Stupid",
        serviceInterface = StupidInterface.class,
        outputPackage = "com.example3")
public @interface StupidAnnotation {
    String value();
}