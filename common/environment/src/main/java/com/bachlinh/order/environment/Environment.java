package com.bachlinh.order.environment;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import com.bachlinh.order.exception.system.environment.EnvironmentException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The system environment object, multiple environment can be store and use in runtime.
 * When environment is requested, this object will look for the properties file to setting
 * environment. Properties file format application-${environment name}.
 *
 * @author Hoang Minh Quan
 */
public final class Environment {
    private static final String PREFIX = "config/application-";
    private static final Map<String, Environment> environments = new ConcurrentHashMap<>();
    private static final ResourceLoader SINGLETON_LOADER = new DefaultResourceLoader();
    private final String environmentName;
    private final Properties properties;

    private Environment(String name) {
        if (name.contains("/")) {
            this.environmentName = name.substring(name.lastIndexOf("/"));
        } else {
            this.environmentName = name;
        }
        String path = name.replace(environmentName, PREFIX.concat(environmentName));
        this.properties = loadProperties(path);
    }

    /**
     * Create environment object with given name and cache it for improved performance.
     * Call this method will cause a problem when it can not find properties file in classpath.
     *
     * @return Environment object.
     */
    public static Environment getInstance(String name) {
        if (!environments.containsKey(name)) {
            environments.put(name, new Environment(name));
        }
        return environments.get(name);
    }

    /**
     * Return system properties with given name or null if name is not existed in environment.
     *
     * @param propertyName Requested properties name.
     * @return System properties value or null if name is not existed.
     */
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName, propertyName);
    }

    /**
     * Return all environment names available in cache.
     *
     * @return All environment names.
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Load system properties from properties file system.
     *
     * @param path Path of system environment properties.
     * @return Properties of system environment.
     */
    private Properties loadProperties(String path) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Properties properties = new Properties();
        String propertiesName = path.concat(".properties");
        try {
            Resource resource = SINGLETON_LOADER.getResource(propertiesName);
            byte[] content = resource.getContentAsByteArray();
            URL url = resource.getURL();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] keyPair = line.split("=");
                if (keyPair[1].contains(",")) {
                    throw new IllegalStateException("Environment does not support multi value on key");
                }
                if (keyPair[1].startsWith("classpath:")) {
                    URL u = SINGLETON_LOADER.getResource(keyPair[1].replace("classpath:", "")).getURL();
                    keyPair[1] = u.toString();
                }
                properties.setProperty(keyPair[0], keyPair[1]);
            }
            reader.close();
        } catch (Exception e) {
            throw new EnvironmentException("Can not load properties file[" + propertiesName + "]", e);
        }
        return properties;
    }
}
