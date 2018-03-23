package org.microcol.gui.mainmenu;

import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;

public class ChangeLanguageEvent {

    private final Text.Language language;

    public ChangeLanguageEvent(final Text.Language language) {
        this.language = Preconditions.checkNotNull(language);
    }

    public Text.Language getLanguage() {
        return language;
    }

}
