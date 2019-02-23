package org.microcol.gui.preferences;

import org.microcol.gui.event.VolumeChangeEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * In UI easy to bind property to some control. This class connect JavaFX
 * property too game properties.
 */
@Singleton
public class PreferencesProperties {

    private final SimpleIntegerProperty animationSpeedProperty;

    private final SimpleIntegerProperty volumeProperty;

    @Inject
    PreferencesProperties(final GamePreferences gamePreferences, final EventBus eventBus) {
        animationSpeedProperty = new SimpleIntegerProperty(gamePreferences.getAnimationSpeed());
        animationSpeedProperty.addListener((object, old, newValue) -> {
            gamePreferences.setAnimationSpeed(newValue.intValue());
        });

        volumeProperty = new SimpleIntegerProperty(gamePreferences.getVolume());
        volumeProperty.addListener((object, old, newValue) -> {
            gamePreferences.setVolume(newValue.intValue());
            eventBus.post(new VolumeChangeEvent(newValue.intValue()));
        });
    }

    public SimpleIntegerProperty getAnimationSpeedProperty() {
        return animationSpeedProperty;
    }

    public SimpleIntegerProperty getVolumeProperty() {
        return volumeProperty;
    }

}
