package org.microcol.gui.event;

import java.util.Locale;

import org.microcol.i18n.I18n;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is raised when user choose to change language.
 */
public final class ChangeLanguageEvent {

    private final I18n i18n;

    //TODO use here Language enum
    public ChangeLanguageEvent(final Locale currentLocale, final I18n i18n) {
        this.i18n = Preconditions.checkNotNull(i18n);
        Preconditions.checkNotNull(currentLocale);
        i18n.setLocale(currentLocale);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ChangeLanguageEvent.class).add("i18n", i18n)
                .add("currentLocale", i18n.getCurrentLocale()).toString();
    }
    
    public Locale getCurrentLocale(){
        return i18n.getCurrentLocale();
    }

    /**
     * @return the i18n
     */
    public I18n getI18n() {
        return i18n;
    }

}
