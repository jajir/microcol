package org.microcol.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.ResourceBundle;

import org.microcol.gui.MicroColException;

public class ResourceBundleProviderXmlImpl implements ResourceBundleProviderXml {

    @Override
    public ResourceBundle getBundle(final String baseName, final Locale locale) {
        Module module = this.getClass().getModule();
        String bundleName = toBundleName(baseName, locale);
        try {
            return loadPropertyResourceBundle(module, bundleName);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    protected String toBundleName(String baseName, Locale locale) {
        return ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
                .toBundleName(baseName, locale);
    }

    private static ResourceBundle loadPropertyResourceBundle(Module module, String bundleName)
            throws IOException {
        String resourceName = toResourceName(bundleName, "xml");
        if (resourceName == null) {
            throw new MicroColException(
                    String.format("Unabble to load xml resource bundle %s in module %s",
                            resourceName, module.getName()));
        }

        PrivilegedAction<InputStream> pa = () -> {
            try {
                return module.getResourceAsStream(resourceName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
        try (InputStream stream = AccessController.doPrivileged(pa)) {
            if (stream != null) {
                return new XmlResourceBundle(stream);
            } else {
                throw new MicroColException(
                        String.format("Unabble to load xml resource bundle %s in module %s",
                                resourceName, module.getName()));
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static String toResourceName(String bundleName, String suffix) {
        if (bundleName.contains("://")) {
            return null;
        }
        StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
        sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
        return sb.toString();
    }

}
