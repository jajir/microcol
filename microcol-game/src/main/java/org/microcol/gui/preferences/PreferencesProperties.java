package org.microcol.gui.preferences;

import org.microcol.gui.event.VolumeChangeEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * In UI easy to bind property to some control. This class connect JavaFX
 * property too game properties.
 */
@Singleton
public class PreferencesProperties {

    private final SimpleIntegerProperty animationSpeedProperty;

    private final SimpleIntegerProperty musicVolumeProperty;

    private final SimpleIntegerProperty effectsVolumeProperty;

    private final SimpleBooleanProperty showFightAdvisorProperty;

    @Inject
    PreferencesProperties(final GamePreferences gamePreferences,
            final EventBus eventBus) {
        animationSpeedProperty = new SimpleIntegerProperty(
                gamePreferences.getAnimationSpeed());
        animationSpeedProperty.addListener((object, old, newValue) -> {
            gamePreferences.setAnimationSpeed(newValue.intValue());
        });

        musicVolumeProperty = new SimpleIntegerProperty(
                gamePreferences.getMusicVolume());
        musicVolumeProperty.addListener((object, old, newValue) -> {
            gamePreferences.setMusicVolume(newValue.intValue());
            eventBus.post(new VolumeChangeEvent(false, newValue.intValue()));
        });

        effectsVolumeProperty = new SimpleIntegerProperty(
                gamePreferences.getEffectsVolume());
        effectsVolumeProperty.addListener((object, old, newValue) -> {
            gamePreferences.setEffectsVolume(newValue.intValue());
            eventBus.post(new VolumeChangeEvent(true, newValue.intValue()));
        });

        showFightAdvisorProperty = new SimpleBooleanProperty();
        showFightAdvisorProperty.setValue(gamePreferences.isShowFightAdvisor());
        showFightAdvisorProperty.addListener((object, old, newValue) -> {
            gamePreferences.setShowFightAdvisor(newValue);
        });
    }

    public SimpleIntegerProperty getAnimationSpeedProperty() {
        return animationSpeedProperty;
    }

    public SimpleIntegerProperty getMusicVolumeProperty() {
        return musicVolumeProperty;
    }

    public SimpleIntegerProperty getEffectsVolumeProperty() {
        return effectsVolumeProperty;
    }

    public SimpleBooleanProperty getShowFightAdvisorProperty() {
        return showFightAdvisorProperty;
    }

}
