package com.jacksonexample;

@RegisteredStringProducer("CONFIGURED")
class ConfiguredStringProducer implements StringProducer {

    private final Configuration configuration;

    public ConfiguredStringProducer(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getString() {
        if (configuration == null) {
            return "<null>";
        }
        return String.format("a=%s;b=%s", configuration.a, configuration.b);
    }

    static final class Configuration {
        private String a;
        private String b;

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public void setA(String a) {
            this.a = a;
        }

        public void setB(String b) {
            this.b = b;
        }
    }
}