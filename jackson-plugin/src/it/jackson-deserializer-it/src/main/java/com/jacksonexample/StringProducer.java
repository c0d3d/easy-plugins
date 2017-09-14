package com.jacksonexample;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = StringProducerRegistry.Deserializer.class)
public interface StringProducer {
    String getString();
}
