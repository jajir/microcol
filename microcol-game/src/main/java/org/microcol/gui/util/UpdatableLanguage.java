package org.microcol.gui.util;

import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;

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
    void updateLanguage(I18n i18n);

    /**
     * Helper method that allows to update language on given method if support
     * it.
     *
     * @param component
     *            required component
     * @param i18n
     *            required internationalization service
     */
    default void tryToUpdateCompoonent(final JavaFxComponent component, final I18n i18n) {
        Preconditions.checkNotNull(component);
        if (component instanceof UpdatableLanguage) {
            ((UpdatableLanguage) component).updateLanguage(i18n);
        }
    }

}
