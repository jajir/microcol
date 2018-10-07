package org.microcol.gui.util;

import org.microcol.i18n.I18n;

/**
 * Declare that JavaFX component contains localized content that should be
 * updated. Content should be updated because user could change language.
 */
public interface UpdatableLanguage {

    /**
     * Method should set correct localization to all content. Method should be
     * called manually from outside.
     *
     * @param i18n
     *            required internationalization service
     */
    void updateLanguage(final I18n i18n);

}
