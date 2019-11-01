package org.microcol.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

import javafx.scene.paint.Color;

/**
 * Class provide color definitions.
 */
@Singleton
public class ColorScheme {

    final Map<String, String> colors = new HashMap<String, String>();

    public ColorScheme() {
        final InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("colors.properties");
        final Properties prop = new Properties();
        try {
            prop.load(stream);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
        for (final String name : prop.stringPropertyNames()) {
            colors.put(name, prop.getProperty(name));
        }
    }

    public Color getColor(final String colorName) {
        Preconditions.checkNotNull(colorName, "Color name is null");
        final String value = colors.get(colorName);
        Preconditions.checkNotNull(value,
                String.format("There is no color definition for '%s'", colorName));
        return Color.web(value);
    }

}
