package org.microcol.gui.event;

import java.util.Locale;

import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is raised when user choose to change language.
 */
public final class ChangeLanguageEvent {

    private final I18n i18n;

    private final Text.Language language;

    public ChangeLanguageEvent(final Text.Language language, final I18n i18n) {
        this.language = Preconditions.checkNotNull(language);
        this.i18n = Preconditions.checkNotNull(i18n);
        i18n.setLocale(language.getLocale());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ChangeLanguageEvent.class).add("i18n", i18n)
                .add("language", language).toString();
    }

    public Text.Language getLanguage() {
        return language;
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
