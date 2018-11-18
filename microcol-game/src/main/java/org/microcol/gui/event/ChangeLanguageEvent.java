package org.microcol.gui.event;

import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;

public final class ChangeLanguageEvent {

    private final I18n i18n;

    private final Text.Language language;

    public ChangeLanguageEvent(final Text.Language language, final I18n i18n) {
        this.language = Preconditions.checkNotNull(language);
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    public Text.Language getLanguage() {
        return language;
    }

    /**
     * @return the i18n
     */
    public I18n getI18n() {
        return i18n;
    }

}
