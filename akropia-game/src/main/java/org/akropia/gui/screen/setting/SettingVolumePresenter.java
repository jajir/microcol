package org.akropia.gui.screen.setting;

import org.akropia.gui.preferences.PreferencesProperties;

import com.google.inject.Inject;

public class SettingVolumePresenter {

    @Inject
    SettingVolumePresenter(final SettingVolumeView view,
            final PreferencesProperties preferencesProperties) {
        view.getMusicVolumeValueProperty().bindBidirectional(
                preferencesProperties.getMusicVolumeProperty());
        view.getEffectsVolumeValueProperty().bindBidirectional(
                preferencesProperties.getEffectsVolumeProperty());
    }

}
