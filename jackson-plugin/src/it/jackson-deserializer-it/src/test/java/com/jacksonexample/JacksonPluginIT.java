package com.jacksonexample;

import org.junit.*;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;

public class JacksonPluginIT {
    @Test
    public void sanityTest() {
        assertEquals("Sanity check", "simple", StringProducerRegistry.getStringProducerByName("SIMPLE").getString());
    }

    @Test
    public void deserializeNoConfigTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringProducer producer = mapper.readValue("\"SIMPLE\"", StringProducer.class);
        assertEquals("Simple deserialization", "simple", producer.getString());
        producer = mapper.readValue("\"CONFIGURED\"", StringProducer.class);
        assertEquals("Configured deserialization", "<null>", producer.getString());
    }

    @Test
    public void deserializeWithConfigTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringProducer producer = mapper.readValue("{\"SIMPLE\": {\"foo\": \"bar\"}}", StringProducer.class);
        assertEquals("Simple deserialization", "simple", producer.getString());
        producer = mapper.readValue("{\"CONFIGURED\": {\"a\": \"thisIsA\", \"b\": \"thisIsB\"}}", StringProducer.class);
        assertEquals("Configured deserialization", "a=thisIsA;b=thisIsB", producer.getString());
    }
}