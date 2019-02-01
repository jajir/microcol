package org.microcol.gui.util;

import java.util.Locale;
import java.util.Optional;

public enum Language {
    
    en(new Locale("en", "US")), cz(new Locale("cs", "CZ"));

    private final Locale locale;

    private Language(final Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public static Optional<Language> resolve(final Locale locale) {
        final String language = locale.getLanguage();
        for (final Language l : values()) {
            if (l.locale.getLanguage().equals(language)) {
                return Optional.of(l);
            }
        }
        return Optional.empty();
    }

}