package org.microcol.gui.event;

import org.microcol.gui.util.Language;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Event is raised when user choose to change language.
 */
public final class ChangeLanguageEvent {

    private Language language;

    public ChangeLanguageEvent(final Language currentLanguage) {
        this.language = Preconditions.checkNotNull(currentLanguage);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ChangeLanguageEvent.class).add("language", language)
                .toString();
    }

    public Language getLanguage() {
        return language;
    }

}
