package com.guiceexample;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Map;

@RegisteredStringProducer("INJECTED_REQUIRED")
public class ForciblyInjectedStringProducer implements StringProducer {

    private StringProducer delegate;

    public ForciblyInjectedStringProducer(Map<String, String> config) {
    }

    @Inject
    public void setDelegate(@Named("RequiredDelegate") StringProducer delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getString() {
        return this.delegate.getString();
    }
}
