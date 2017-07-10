package com.nlocketz;

import javax.annotation.processing.RoundEnvironment;
import java.util.Set;

/**
 * All our processing functions will throw this with the correct error message that needs to be delivered to the user.
 * I'm not a fan of custom exceptions, but in this case we use it to differentiate exceptions that should result
 * in error messages to the user, and exception which result from bugs inside our code.
 * @see FeatureRegistryProcessor#process(Set, RoundEnvironment)
 */
class EZServiceException extends RuntimeException {
    public EZServiceException(String s) {
        super(s);
    }
}
