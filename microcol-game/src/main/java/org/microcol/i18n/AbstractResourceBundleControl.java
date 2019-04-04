package org.microcol.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public abstract class AbstractResourceBundleControl extends ResourceBundle.Control {

    private final ResourceBundleFormat resourceBundleFormat;

    public AbstractResourceBundleControl(final ResourceBundleFormat format) {
        this.resourceBundleFormat = Objects.requireNonNull(format,
                "Resource bundle format can't be null.");
    }

    @Override
    public List<String> getFormats(final String baseName) {
        Objects.requireNonNull(baseName, "base name can't be null");
        return Arrays.asList(resourceBundleFormat.name());
    }

    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale, final String format,
            final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        Objects.requireNonNull(baseName, "base name can't be null");
        Objects.requireNonNull(locale, "locale can't be null");
        Objects.requireNonNull(format, "format can't be null");
        Objects.requireNonNull(loader, "loader can't be null");

        if (format.equals(resourceBundleFormat.name())) {
            final String bundleName = toBundleName(baseName, locale);
            final String resourceName = toResourceName(bundleName, format);
            final URL url = loader.getResource(resourceName);
            if (url == null) {
                throw new MissingResourceException(
                        String.format("Cant find file '%s'", resourceName), resourceName, "");
            } else {
                final URLConnection connection = url.openConnection();
                if (connection == null) {
                    throw new MissingResourceException(
                            String.format("Can't open connection to '%s'.", resourceName),
                            resourceName, "");
                } else {
                    if (reload) {
                        // disable caches if reloading
                        connection.setUseCaches(false);
                    }
                    try (InputStream stream = connection.getInputStream()) {
                        return createResourceBundleFrom(stream, resourceName);
                    }
                }
            }
        }
        return null;
    }

    abstract ResourceBundle createResourceBundleFrom(InputStream stream, String resourceName)
            throws IOException;

}
