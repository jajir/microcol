package org.microcol.gui.event;

import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@Listener
public final class ChangeLanguageListenerPreferences {

    private final GamePreferences gamePreferences;

    @Inject
    public ChangeLanguageListenerPreferences(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    public void onEvent(final ChangeLanguageEvent event) {
        gamePreferences.setLanguage(event.getCurrentLocale());
    }

}
