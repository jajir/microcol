package org.microcol.i18n;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * Class helps to load XML resource bundle to java SE {@link ResourceBundle}.
 */
public class XmlResourceBundleControl extends AbstractResourceBundleControl {

    public XmlResourceBundleControl() {
        super(ResourceBundleFormat.xml);
    }

    @Override
    ResourceBundle createResourceBundleFrom(InputStream stream, String resourceName)
            throws IOException {
        return new XmlResourceBundle(new BufferedInputStream(stream));
    }
}
