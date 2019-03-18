package com.linkedkeeper.spi;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionLoader implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private ApplicationContext context;

    private static final ConcurrentMap<Class<?>, Map<String, Object>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final String SERVICES_DIRECTORY = "META-INF/spi/";

    public Map<String, Object> getExtensionLoader(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }
        Map<String, Object> loader = EXTENSION_LOADERS.get(type);
        if (loader == null) {
            synchronized (ExtensionLoader.class) {
                loader = EXTENSION_LOADERS.get(type);
                if (loader == null) {
                    EXTENSION_LOADERS.putIfAbsent(type, loadExtensionClass(type.getName()));
                    loader = EXTENSION_LOADERS.get(type);
                }
            }
        }
        return loader;
    }

    private Map<String, Object> loadExtensionClass(String type) {
        Map<String, Object> extensionClasses = new HashMap<>();
        loadDirectory(extensionClasses, SERVICES_DIRECTORY, type);
        return extensionClasses;
    }

    private void loadDirectory(Map<String, Object> extensionClasses, String dir, String type) {
        String fileName = dir + type;
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourcesURL = urls.nextElement();
                    loadResources(extensionClasses, classLoader, resourcesURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private void loadResources(Map<String, Object> extensionClasses, ClassLoader classLoader, URL resourceURL) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) {
                        line = line.substring(0, ci);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class (class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred when loading extension class (class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    private static ClassLoader findClassLoader() {
        return DefaultListableBeanFactory.class.getClassLoader();
    }

    private void loadClass(Map<String, Object> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalStateException("No such extension name for the class " + name + " in the config " + resourceURL);
        }
        saveInExtensionClass(extensionClasses, clazz, name);
    }

    private void saveInExtensionClass(Map<String, Object> extensionClasses, Class<?> clazz, String name) {
        Object o = extensionClasses.get(name);
        if (o == null) {
            Object bean = parseClassToSpringBean(name, clazz);
            extensionClasses.put(name, bean);
        } else {
            throw new IllegalStateException("Duplicate extension name " + name + " on " + clazz.getName() + " and " + clazz.getName());
        }
    }

    private Object parseClassToSpringBean(String name, Class<?> obj) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(obj);
        GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
        definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_NAME);
        getRegistry().registerBeanDefinition(name, definition);

        return context.getBean(name);
    }

    public BeanDefinitionRegistry getRegistry() {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) context;
        return (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
