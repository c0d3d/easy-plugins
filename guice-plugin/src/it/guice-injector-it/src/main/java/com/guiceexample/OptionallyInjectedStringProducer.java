package com.guiceexample;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Map;

@RegisteredStringProducer("INJECTED_OPTIONAL")
public class OptionallyInjectedStringProducer implements StringProducer {

    private String toSay;
    // Delegate will override any configured thing to say
    private StringProducer delegate;

    public OptionallyInjectedStringProducer(Map<String, String> config) {
        if (config.containsKey("toSay")) {
            this.toSay = config.get("toSay");
        } else {
            this.toSay = "<unconfigured>";
        }
    }

    @Inject(optional = true)
    public void setDelegate(@Named("OptionalDelegate") StringProducer delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getString() {
        if (this.delegate == null) {
            return this.toSay;
        } else {
            return this.delegate.getString();
        }
    }
}
