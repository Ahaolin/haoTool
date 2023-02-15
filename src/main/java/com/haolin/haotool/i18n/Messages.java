package com.haolin.haotool.i18n;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Messages {
    private final static Map<Locale, AggregateBundle> resourceBundles = new ConcurrentHashMap<>();
    private static final String PROP_PATH = "i18n/";
    private static final String PROP_FILE_NAME = "messages";
    private static final String PROP_FILE_POSTFIX = "messages";

    static {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(PROP_PATH);
            URL url = null;
            while (resources.hasMoreElements()) {
                url = resources.nextElement();
                addUrl(url);
            }
            url = classLoader.getResource(PROP_PATH);
            addUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addUrl(URL url) {
        if ("jar".equals(url.getProtocol())) {
            String file;
            JarFile jarFile = null;
            try {
                URLConnection conn = url.openConnection();
                JarURLConnection jarConn = (JarURLConnection) conn;
                jarFile = jarConn.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    file = entry.getName();
                    int lastIndexOf = file.lastIndexOf(".");
                    if (lastIndexOf > 0) {
                        String postfix = file.substring(lastIndexOf + 1);
                        if (file.startsWith("i18n/") && "properties".equals(postfix)) {
                            file = file.substring(5, file.length() - 11);
                            if (file.length() > 8) {
                                String lang = file.substring(9);
                                Locale locale = getLocal(lang);
                                addResource(url, locale);
                            } else {
                                addResource(url, Locale.ROOT);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IoUtil.close(jarFile);
            }
            return;
        }
        File path = new File(url.getFile());
        String[] list = path.list();
        if (ArrayUtil.isEmpty(list)) return;
        for (String data : list) {
            data = data.substring(0, data.length() - 11);
            if (data.length() > 8) {
                String lang = data.substring(9);
                Locale locale = getLocal(lang);
                addResource(url, locale);
            } else
                addResource(url, Locale.ROOT);
        }
    }

    private static void addResource(URL url, Locale locale) {
        AggregateBundle aggregateBundle = resourceBundles.get(locale);
        if (aggregateBundle == null) {
            aggregateBundle = new AggregateBundle();
            resourceBundles.put(locale, aggregateBundle);
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        ResourceBundle resourceBundle;
        try {
            resourceBundle = ResourceBundle.getBundle("messages", locale, classLoader);
        } catch (MissingResourceException e) {
            resourceBundle = ResourceBundle.getBundle("messages", Locale.ROOT, classLoader);
        }
        aggregateBundle.addBundle(resourceBundle);
    }

    private static void addResource(Map<String, Object> data, Locale locale) {
        AggregateBundle aggregateBundle = resourceBundles.get(locale);
        if (aggregateBundle == null) {
            aggregateBundle = new AggregateBundle();
            resourceBundles.put(locale, aggregateBundle);
        }
        aggregateBundle.addData(data);
    }

    private static Locale getLocal(String lang) {
        if (StrUtil.isNotBlank(lang)){
            String[] split = lang.split("_");
            return split.length > 1 ? new Locale(split[0],split[1]) : new Locale(split[0]);
        }
        return Locale.getDefault();
    }

    public static String getMessage(String key, Object... args) {
        Locale china = Locale.CHINA;
        return getMessage(key, china, args);
    }

    public static String getMessage(String key,Locale locale, Object... args) {
       try {
           AggregateBundle aggregateBundle = resourceBundles.get(locale);
           if (null == aggregateBundle) return null;
           String string = aggregateBundle.getString(key);
           return MessageFormat.format(string,args);
       }catch (Exception e){
            e.printStackTrace();
           return key;
       }
    }

    public static class IteratorEnumeration<T> implements Enumeration<T> {
        private final Iterator<T> source;

        public IteratorEnumeration(Iterator<T> source) {
            if (source == null) {
                throw new IllegalArgumentException("Source must not be null");
            } else {
                this.source = source;
            }
        }

        @Override
        public boolean hasMoreElements() {
            return this.source.hasNext();
        }

        @Override
        public T nextElement() {
            return this.source.next();
        }
    }

    public static class AggregateBundle extends ResourceBundle {
        private final Map<String, Object> contents = new HashMap<>();

        public AggregateBundle() {
        }

        @Override
        protected Object handleGetObject(String key) {
            return this.contents.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return new IteratorEnumeration<>(this.contents.keySet().iterator());
        }

        public void addBundle(ResourceBundle bundle) {
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String oneKey = keys.nextElement();
                this.contents.put(oneKey, bundle.getObject(oneKey));
            }
        }

        public void addData(Map<String, Object> data) {
            this.contents.putAll(data);
        }

    }

}
