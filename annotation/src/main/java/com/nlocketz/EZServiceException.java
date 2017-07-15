package com.nlocketz;

import com.nlocketz.EasyServiceProcessor;

import javax.annotation.processing.RoundEnvironment;
import java.util.Set;

/**
 * All our processing functions will throw this with the correct error message that needs to be delivered to the user.
 * I'm not a fan of custom exceptions, but in this case we use it to differentiate exceptions that should result
 * in error messages to the user, and exceptions which result from bugs inside our code.
 * @see EasyServiceProcessor#process(Set, RoundEnvironment)
 */
public class EZServiceException extends RuntimeException {
    public EZServiceException(String s) {
        super(s);
    }
}
