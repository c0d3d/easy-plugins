# Easy Plugins [![Build Status](https://travis-ci.org/c0d3d/easy-plugins.svg?branch=master)](https://travis-ci.org/c0d3d/easy-plugins) [![Maven Central](https://img.shields.io/maven-central/v/com.nlocketz.plugins/easy-plugins.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.nlocketz.plugins%22%20a%3A%22easy-plugins%22)

Easy plugins is a library that lets you easily develop using a seamless service based architecture in Java!

## Example

To use easy plugins simply annotate an annotation that you write with `@Service`.
`@Service` has three required values and one optional one.
They are:
1. `value`: This is the name of the service you are creating
2. `serviceInterface`: This the `Class` object for the interface that each of your providers must implement. Abstract classes are also permitted.
3. `outputPackage`: This is the output package for all the generated classes used to support your new services.
4. (optional) `serviceNameKey`: This is the name of the field inside the annotation the `@Service` is annotating to look for the name of each service provider.

Here is an example usage:

	import com.nlocketz.Service;

	@Service(value = "AdderService",
		 serviceInterface = Adder.class,
		 outputPackage = "com.nlocketz.adders.generated")
	public @interface AdderService {
		String value();
	}

This example assumes that you have a class named `Adder` which easy-plugins will use to generate the service interface.
Here is our interface:

	public interface Adder {
		int add(int x, int y);
	}

That is all you need to do to create a new service!

The problem now is you don't have any actual `Adder`s to use, so let's make one.

	@AdderService("AccurateAdder")
	public class Adder1 implements Adder {
		@Override
		public int add(int x, int y) {
			return x+y;
		}
	}

and another one.

	@AdderService("InaccurateAdder")
	public class Adder2 implements Adder {
		@Override
		public int add(int x, int y) {
			return x - y;
		}
	}

Now you have a service with two providers named `AccurateAdder`, and `InaccurateAdder`.

To access them you use the class `AdderServiceRegistry` which has been generated as a result of annotating `AdderService` with `@Service`.

Here we use both to add two numbers:

	Adder accurate = AdderServiceRegistry.getAdderServiceByName("AccurateAdder");
	Adder inaccurate = AdderServiceRegistry.getAdderServiceByName("InaccurateAdder");
	assertEquals(3, accurate.add(1, 2));
	assertEquals(-1, inaccurate.add(1, 2));

`AdderServiceRegistry` also contains another static method called `getAdderServiceByNameWithConfig` which takes a name and a `Map<String, String>` which it
pass to your provider as a kind of configuration.

Your provider can choose to subscribe to these configuration calls by created a constructor that consumes a `Map<String, String>`. If you don't have one, the default constructor will be called.
If you don't have a default constructor and `getAdderServiceByName` is called `Collections.emptyMap()` will be provided as the argument to the `Map<String, String>` constructor.

Here is an example `Adder` that uses configuration information:

	import java.util.Map;
	import com.nlocketz.adders.api.Adder;
	import com.nlocketz.adders.api.AdderService;

	@AdderService("OffsetAdder")
	public class Adder4 implements Adder {
	    private int offset;

	    public Adder4(Map<String, String> config) {
            offset = Integer.parseInt(config.get("offset"));
	    }


	    @Override
        public int add(int x, int y) {
            return y + x + offset;
	    }
	}

and the usage is like this:

	Adder offset = AdderServiceRegistry.getAdderServiceByNameWithConfig("OffsetAdder",
		Collections.singletonMap("offset", "3"));
	Adder accurate = AdderServiceRegistry.getAdderServiceByNameWithConfig("AccurateAdder",
		Collections.singletonMap("offset", "3"));
	Adder inaccurate = AdderServiceRegistry.getAdderServiceByNameWithConfig("InaccurateAdder",
		Collections.singletonMap("offset", "3")););
	assertEquals(3, accurate.add(1, 2));
	assertEquals(-1, inaccurate.add(1, 2));
	assertEquals(3, offset.add(0, 0));
