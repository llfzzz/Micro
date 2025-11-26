package com.micro.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads configuration files from the classpath.
 */
public final class PropertyUtil {

    private PropertyUtil() {
    }

    public static Properties load(String resourceName) {
        Objects.requireNonNull(resourceName, "resourceName");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = loader.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Cannot find resource " + resourceName);
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load properties from " + resourceName, ex);
        }
    }
}
