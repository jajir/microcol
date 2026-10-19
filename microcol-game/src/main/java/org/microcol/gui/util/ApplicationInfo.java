package org.microcol.gui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.microcol.model.MicroColException;

import com.google.inject.Singleton;

/**
 * Class provide actual information about running application.
 */
@Singleton
public final class ApplicationInfo {

    private final static String VERSION_PROPERTIES_FILE = "version.properties";

    private final static String VERSION_KEY = "version";

    /**
     * Formats the build ID string from properties, appending it to the version
     * if present.
     *
     * @param props Properties object containing 'version' and optionally
     *              'build.id'
     * @return formatted version string including build ID suffix if available
     */
    static String formatBuildVersion(final Properties props) {
        final String version = props.getProperty(VERSION_KEY);
        final String buildId = props.getProperty("build.id", "").trim();

        // Return version unchanged if build.id is missing, blank, 'local', or
        // unresolved placeholder
        if (buildId.isEmpty() || "local".equals(buildId)
                || buildId.contains("${")) {
            return version;
        }

        return version + " (" + buildId + ")";
    }

    /**
     * Build identity for diagnostics. Missing metadata must not stop startup.
     *
     * @return build version, or <code>unknown</code> when it cannot be read
     */
    public String getDiagnosticVersion() {
        try {
            return getBuildVersion();
        } catch (RuntimeException unavailable) {
            return "unknown";
        }
    }

    public String getBuildVersion() {
        final Properties prop = new Properties();
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(VERSION_PROPERTIES_FILE)) {
            prop.load(stream);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
        return formatBuildVersion(prop);
    }

}
