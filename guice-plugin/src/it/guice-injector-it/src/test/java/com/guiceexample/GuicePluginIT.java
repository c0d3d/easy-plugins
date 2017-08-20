package com.guiceexample;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GuicePluginIT {
    @Test
    public void testStringProducer() {
        StringProducer sp = StringProducerRegistry.getStringProducerByName("UNINJECTED");
        assertEquals("Uninjected string producer", "Hello, World", sp.getString());
        sp = StringProducerRegistry.getStringProducerByName("INJECTED_OPTIONAL");
        assertEquals("Optionally injected unconfigured uninjected string producer", "<unconfigured>", sp.getString());
        Map<String, String> config = new HashMap<>();
        config.put("toSay", "foobar");
        sp = StringProducerRegistry.getStringProducerByNameWithConfig("INJECTED_OPTIONAL", config);
        assertEquals("Optionally injected configured uninjected string producer", "foobar", sp.getString());

        Module module = new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(StringProducer.class)
                        .annotatedWith(Named.class) // needed to work as a catch-all for @Named annotations
                        .to(InjectedA.class);
                binder.bind(StringProducer.class)
                        .to(InjectedA.class);
            }
        };

        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_OPTIONAL", Guice.createInjector(module));
        assertEquals("Optionally injected unconfigured injected string producer", "Injected A", sp.getString());
        sp = StringProducerRegistry.getInjectedStringProducerByNameWithConfig("INJECTED_OPTIONAL", config, Guice.createInjector(module));
        assertEquals("Optionally injected configured injected string producer", "Injected A", sp.getString());
        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_REQUIRED", Guice.createInjector(module));
        assertEquals("Forcibly injected string producer (solo)", "Injected A", sp.getString());

        module = new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(StringProducer.class)
                        .annotatedWith(Names.named("OptionalDelegate"))
                        .to(InjectedA.class);
                binder.bind(StringProducer.class)
                        .annotatedWith(Names.named("RequiredDelegate"))
                        .to(InjectedB.class);
            }
        };

        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_OPTIONAL", Guice.createInjector(module));
        assertEquals("Optional named binding", "Injected A", sp.getString());
        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_REQUIRED", Guice.createInjector(module));
        assertEquals("Required named binding", "Injected B", sp.getString());

        module = new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(StringProducer.class)
                        .annotatedWith(Names.named("RequiredDelegate"))
                        .to(InjectedB.class);
            }
        };
        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_OPTIONAL", Guice.createInjector(module));
        assertEquals("Optional named binding (unbound)", "<unconfigured>", sp.getString());
        sp = StringProducerRegistry.getInjectedStringProducerByName("INJECTED_REQUIRED", Guice.createInjector(module));
        assertEquals("Required named binding (solo)", "Injected B", sp.getString());
    }

    private static class InjectedA implements StringProducer {
        @Override
        public String getString() {
            return "Injected A";
        }
    }

    private static class InjectedB implements StringProducer {
        @Override
        public String getString() {
            return "Injected B";
        }
    }
}
