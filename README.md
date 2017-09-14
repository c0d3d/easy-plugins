# Easy Plugins [![Build Status](https://travis-ci.org/c0d3d/easy-plugins.svg?branch=master)](https://travis-ci.org/c0d3d/easy-plugins) [![Maven Central](https://img.shields.io/maven-central/v/com.nlocketz.plugins/easy-plugins.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.nlocketz.plugins%22%20a%3A%22easy-plugins%22)

`easy-plugins` is a library that lets you easily develop using a seamless service based architecture in Java!

## Example

To use easy plugins simply annotate an annotation that you write with `@Service`.
`@Service` has three required values and one optional one.
They are:
1. `value`: This is the name of the service you are creating
2. `serviceInterface`: This the `Class` object for the interface that each of your providers must implement. Abstract classes are also permitted.
3. `outputPackage`: This is the output package for all the generated classes used to support your new services.
4. (optional) `serviceNameKey`: This is the name of the field inside the annotation the `@Service` is annotating to look for the name of each service provider.

Here is an example usage:

```java
import com.nlocketz.Service;

@Service(value = "AdderService",
	 serviceInterface = Adder.class,
	 outputPackage = "com.nlocketz.adders.generated")
public @interface AdderService {
	String value();
}
```

This example assumes that you have a class named `Adder` which easy-plugins will use to generate the service interface.
Here is our interface:

```java
public interface Adder {
	int add(int x, int y);
}
```

That is all you need to do to create a new service!

The problem now is you don't have any actual `Adder`s to use, so let's make one.

```java
@AdderService("AccurateAdder")
public class Adder1 implements Adder {
	@Override
	public int add(int x, int y) {
		return x+y;
	}
}
```

and another one.

```java
@AdderService("InaccurateAdder")
public class Adder2 implements Adder {
	@Override
	public int add(int x, int y) {
		return x - y;
	}
}
```

Now you have a service with two providers named `AccurateAdder`, and `InaccurateAdder`.

To access them you use the class `AdderServiceRegistry` which has been generated as a result of annotating `AdderService` with `@Service`.

Here we use both to add two numbers:

```java
Adder accurate = AdderServiceRegistry.getAdderServiceByName("AccurateAdder");
Adder inaccurate = AdderServiceRegistry.getAdderServiceByName("InaccurateAdder");
assertEquals(3, accurate.add(1, 2));
assertEquals(-1, inaccurate.add(1, 2));
```

`AdderServiceRegistry` also contains another static method called `getAdderServiceByNameWithConfig` which takes a name and an `Object` which it
pass to your provider as a kind of configuration.

Your provider can choose to subscribe to these configuration calls by created a constructor that consumes an argument. If you don't have one, the default constructor will be called.
If you don't have a default constructor and `getAdderServiceByName` is called, a default value (an empty map for `Map`, and empty list for `List`, `0` for numbers, `false` for
booleans, and `null` for other objects) will be provided as the argument to the one-argument constructor. Annotating a constructor with `@ConfigurationConstructor` will override this
behavior and cause that constructor to be used when creating with a configuration.

Here is an example `Adder` that uses configuration information:

```java
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
```

and the usage is like this:

```java
Adder offset = AdderServiceRegistry.getAdderServiceByNameWithConfig("OffsetAdder",
	Collections.singletonMap("offset", "3"));
Adder accurate = AdderServiceRegistry.getAdderServiceByNameWithConfig("AccurateAdder",
	Collections.singletonMap("offset", "3"));
Adder inaccurate = AdderServiceRegistry.getAdderServiceByNameWithConfig("InaccurateAdder",
	Collections.singletonMap("offset", "3")););
assertEquals(3, accurate.add(1, 2));
assertEquals(-1, inaccurate.add(1, 2));
assertEquals(3, offset.add(0, 0));
```

## Plugins

`easy-plugins` also has an experimental plugin API of its own, which can be used to extend the
behavior of generated classes. For example, the [Guice Plugin](guice-plugin) extends the framework
to include methods which accept Guice injectors. For example, one can do the following:

```java
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nlocketz.adders.api.Adder;
import com.nlocketz.adders.api.AdderService;

@AdderService("OffsetAdder")
public class AdderN implements Adder {
    private int offset;

    public Adder4(int offset) {
        this.offset = offset;
    }

    @Inject(optional = true)
    public void setOffset(@Named("offset") Integer offset) {
        this.offset = offset;
    }

    @Override
    public int add(int x, int y) {
        return y + x + offset;
    }
}
```

Additionally, there is a [Jackson Plugin](jackson-plugin), which adds a Jackson deserializer to
the generated registry. For example, the above class can be deserialized from the following JSON
string (corresponding to a construction with no configuration):
```
"OffsetAdder"
```
If a configuration is desired, it can be used using the configuration type's JSON serialization:
```
{"OffsetAdder": 5}
```