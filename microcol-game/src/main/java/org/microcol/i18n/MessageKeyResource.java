package org.microcol.i18n;

import java.util.ResourceBundle;

/**
 * It's marker interface which inform that enum holds message key resources.
 */
public interface MessageKeyResource {

    default public ResourceBundle.Control getResourceBundleControl() {
        return null;
    }
}
