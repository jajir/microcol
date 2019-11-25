package org.microcol.gui.screen.menu;

import org.microcol.gui.preferences.GamePreferences;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provide information if secret option key was pressed. Could be useful for
 * debug operations.
 * <p>
 * Secret option could be enable just when development mode is enabled.
 * </p>
 */
@Singleton
public class SecretOption {

    private final GamePreferences gamePreferences;

    private boolean enabled = false;

    @Inject
    SecretOption(final GamePreferences gamePreferences) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    public boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        if (enabled) {
            if (gamePreferences.isDevelopment()) {
                this.enabled = true;
            }
        } else {
            this.enabled = false;
        }
    }

}
