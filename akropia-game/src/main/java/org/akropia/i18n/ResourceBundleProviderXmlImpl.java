package org.akropia.i18n;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akropia.gui.util.StreamReader;
import org.akropia.model.AkropiaException;

import com.google.common.base.Preconditions;

class ResourceBundleProviderXmlImpl implements ResourceBundleProviderXml {

    private final StreamReader streamReader;

    ResourceBundleProviderXmlImpl(final StreamReader streamReader) {
        this.streamReader = Preconditions.checkNotNull(streamReader);
    }

    @Override
    public ResourceBundle getBundle(final String baseName,
            final Locale locale) {
        Module module = this.getClass().getModule();
        String bundleName = toBundleName(baseName, locale);
        try {
            return loadPropertyResourceBundle(module, bundleName);
        } catch (IOException e) {
            throw new AkropiaException(e.getMessage(), e);
        }
    }

    private String toBundleName(String baseName, Locale locale) {
        return ResourceBundle.Control
                .getControl(ResourceBundle.Control.FORMAT_DEFAULT)
                .toBundleName(baseName, locale);
    }

    private ResourceBundle loadPropertyResourceBundle(Module module,
            String bundleName) throws IOException {
        final String resourceName = streamReader.toResourceName(bundleName,
                "xml");
        if (resourceName == null) {
            throw new AkropiaException(String.format(
                    "Unabble to load xml resource bundle %s in module %s",
                    resourceName, module.getName()));
        }

        return new XmlResourceBundle(streamReader.openStream(resourceName));
    }

}
