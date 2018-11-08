package org.microcol.gui.event;

import org.microcol.gui.mainmenu.AnimationSpeedChangeEvent;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Listener;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Class just pass changed animation speed to preferences.
 */
@Listener
public final class AnimationSpeedChangedListenerPreferences {

    private final GamePreferences gamePreferences;

    @Inject
    public AnimationSpeedChangedListenerPreferences(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    @Subscribe
    private void onAnimationSpeedChange(final AnimationSpeedChangeEvent event) {
        gamePreferences.setAnimationSpeed(event.getAnimationSpeed());
    }

}
