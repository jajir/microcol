package org.microcol.gui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.microcol.gui.MicroColException;

import com.google.inject.Singleton;

/**
 * Class provide actual information about running application.
 */
@Singleton
public final class ApplicationInfo {

    private final static String VERSION_PROPERTIES_FILE = "version.properties";

    private final static String VERSION_KEY = "version";

    public String getBuildVersion() {
        final InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(VERSION_PROPERTIES_FILE);
        final Properties prop = new Properties();
        try {
            prop.load(stream);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
        return prop.getProperty(VERSION_KEY);
    }

}
