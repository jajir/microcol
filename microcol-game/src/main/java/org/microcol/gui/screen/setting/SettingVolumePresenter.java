package org.microcol.gui.screen.setting;

import org.microcol.gui.preferences.PreferencesProperties;

import com.google.inject.Inject;

public class SettingVolumePresenter {

    @Inject
    SettingVolumePresenter(final SettingVolumeView view,
            final PreferencesProperties preferencesProperties) {
        view.getVolumeValueProperty().bindBidirectional(preferencesProperties.getVolumeProperty());
    }

}
