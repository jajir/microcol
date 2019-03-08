package org.microcol.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonResourceBundle extends ResourceBundle {

    private Properties props;

    JsonResourceBundle(final InputStream stream) throws IOException {
        // TODO use in a static way, don't use new instance in each bundle
        final Gson gson = new GsonBuilder().create();
        props = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            ResourceBundleModel resourceBundleModel = gson.fromJson(reader,
                    ResourceBundleModel.class);
            resourceBundleModel.getMessages().stream().filter(pair -> pair.getKey() != null)
                    .forEach(pair -> props.setProperty(pair.getKey(), pair.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Object handleGetObject(final String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return props.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        final List<String> keys = props.keySet().stream().map(obj -> (String) obj)
                .collect(Collectors.toList());
        return new Enumeration<String>() {

            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < keys.size();
            }

            @Override
            public String nextElement() {
                return keys.get(index++);
            }
        };
    }
}