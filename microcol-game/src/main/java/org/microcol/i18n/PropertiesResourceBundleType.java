package org.microcol.i18n;

import java.util.ResourceBundle.Control;

public class PropertiesResourceBundleType extends AbstractResourceBundleType {

    PropertiesResourceBundleType() {
        super(ResourceBundleFormat.properties.name());
    }

    @Override
    Control getResourceBundleControl() {
        return null;
    }

}
