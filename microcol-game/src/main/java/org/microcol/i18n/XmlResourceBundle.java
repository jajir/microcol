package org.microcol.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class XmlResourceBundle extends ResourceBundle {

    private Properties props;

    XmlResourceBundle(final InputStream stream) throws IOException {
        props = new Properties();
        props.loadFromXML(stream);
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