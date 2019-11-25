package org.microcol.i18n;

import java.util.ResourceBundle.Control;

class JsonResourceBundleType extends AbstractResourceBundleType {

    JsonResourceBundleType() {
        super(ResourceBundleFormat.json.name());
    }

    @Override
    Control getResourceBundleControl() {
        return new JsonResourceBundleControl();
    }

}
