package org.microcol.i18n;

import java.util.ResourceBundle.Control;

public class XmlResourceBundleType extends AbstractResourceBundleType {

    XmlResourceBundleType() {
        super(ResourceBundleFormat.xml.name());
    }

    @Override
    Control getResourceBundleControl() {
        return new XmlResourceBundleControl();
    }

}
