package org.microcol.i18n;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * Class helps to load XML resource bundle to java SE {@link ResourceBundle}.
 */
class JsonResourceBundleControl extends AbstractResourceBundleControl {

    JsonResourceBundleControl() {
        super(ResourceBundleFormat.json);
    }

    @Override
    ResourceBundle createResourceBundleFrom(InputStream stream, String resourceName)
            throws IOException {
        return new JsonResourceBundle(new BufferedInputStream(stream));
    }
}
