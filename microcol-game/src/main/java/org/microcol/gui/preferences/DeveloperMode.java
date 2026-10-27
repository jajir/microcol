package org.microcol.gui.preferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/** Shared, persisted runtime diagnostics; independent of launch-time tools. */
@Singleton
public final class DeveloperMode {
    private final SettingService settings;
    private final ReadOnlyBooleanWrapper enabled;

    @Inject
    public DeveloperMode(final SettingService settings) {
        this.settings = settings;
        enabled = new ReadOnlyBooleanWrapper(settings.load().isDeveloperMode());
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public ReadOnlyBooleanProperty enabledProperty() {
        return enabled.getReadOnlyProperty();
    }

    public void setEnabled(final boolean value) {
        if (value != isEnabled()) {
            // Publish only after persistence succeeds, keeping all controls in
            // sync.
            settings.update(setting -> setting.setDeveloperMode(value));
            enabled.set(value);
        }
    }
}
