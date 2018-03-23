package org.microcol.gui.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.microcol.gui.MicroColException;

/**
 * Class provide actual information about running application.
 */
public class ApplicationInfo {

    private final static String DEFAULT_APPLICATION_VERSION = "SNAPSHOT";

    private final static String APPLICATION_VERSION_KEY = "Implementation-Version";

    public String getVersion() {
        try {
            return tryToFindVersion();
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    private String tryToFindVersion() throws MalformedURLException, IOException {
        final Class<ApplicationInfo> clazz = ApplicationInfo.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            return DEFAULT_APPLICATION_VERSION;
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();
        return attr.getValue(APPLICATION_VERSION_KEY);
    }

}
