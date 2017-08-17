package com.nlocketz.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import java.io.IOException;
import java.util.*;

public class ProcessorOutputCollection {

    private ProcessorOutputCollection() {

    }

    private Map<String, Set<String>> serviceProviders = new HashMap<>();
    private Map<String, Set<TypeSpec>> outputClasses = new HashMap<>();

    private static <T> void addToMultiMap(Map<String, Set<T>> map, String key, T value, String msg) {
        if (map.containsKey(key)) {

            if (map.get(key).contains(value) && msg != null) {
                // Prevent duplicate creations
                throw new EasyPluginException(msg + value.toString());
            }

            map.get(key).add(value);
        } else {
            HashSet<T> h = new HashSet<>();
            h.add(value);
            map.put(key, h);
        }
    }

    /**
     * Adds a {@link TypeSpec} to the set of files that will be generated at the end of the build pipeline.
     * The {@link TypeSpec} will be put into the file under the qualified name {@code pkgName}.
     * Will also add a service entry in each of the service classes listed in {@code providesServicesFor}
     * @param pkgName The fully qualified name of the type to be created.
     * @param newType the new type to be created at the end of the pipeline.
     * @param providesServicesFor A list of classes that the given type provides the service for.
     */
    public void putType(String pkgName, TypeSpec newType, List<ClassName> providesServicesFor) {

        // This prevents us from opening the same file twice with the filer
        // as well as letting the user know that the generated two files with the same name
        addToMultiMap(outputClasses, pkgName, newType, "Attempted to generate 2 classes with the same name: ");

        if (!providesServicesFor.isEmpty()) {
            for (ClassName serviceInterface : providesServicesFor) {
                String interfaceQName = serviceInterface.reflectionName();
                // Here it is ok to have dups (but won't happen because of the same call above)
                addToMultiMap(serviceProviders, interfaceQName, pkgName + "." + newType.name, null);
            }
        }
    }

    public void putType(String outputPackage, TypeSpec type) {
        putType(outputPackage, type, Collections.<ClassName>emptyList());
    }

    public static ProcessorOutputCollection empty() {
        return new ProcessorOutputCollection();
    }

    public void writeContents(Filer filer) {
        for (Map.Entry<String, Set<TypeSpec>> newTypes : outputClasses.entrySet()) {
            for (TypeSpec type : newTypes.getValue()) {
                writeFile(filer,
                        JavaFile.builder(newTypes.getKey(), type).build());
            }
        }

        for (Map.Entry<String, Set<String>> service : serviceProviders.entrySet()) {
            Util.writeMetaInfServices(service.getKey(), service.getValue(), filer);
        }
    }


    private void writeFile(Filer filer, JavaFile file) {
        try {
            file.writeTo(filer);
        } catch (FilerException e) {
            throw new EasyPluginException("Couldn't create file: "+e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
