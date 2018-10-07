package org.microcol.gui.event;

import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Text;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class connect changing of language to resource bundle. When language is
 * changed than class force {@link Text} to re-load resource bundle.
 */
public final class ChangeLanguageListenerText implements Listener<ChangeLanguageEvent> {

    private final Text text;
    
    private final I18n i18n;
    
    @Inject
    public ChangeLanguageListenerText(final Text text,final I18n i18n,
            final ChangeLanguageController languangeController) {
        this.text = Preconditions.checkNotNull(text);
        this.i18n = Preconditions.checkNotNull(i18n);
        languangeController.addListener(this, 1);
    }

    @Override
    public void onEvent(final ChangeLanguageEvent event) {
        text.setLocale(event.getLanguage().getLocale());
        i18n.setLocale(event.getLanguage().getLocale());
    }

}
