package org.microcol.gui.screen.setting;

import org.microcol.gui.preferences.PreferencesProperties;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingAnimationSpeedPresenter {

    @Inject
    SettingAnimationSpeedPresenter(final SettingAnimationSpeedView view,
            final PreferencesProperties preferencesProperties) {
        view.getAnimationSpeedValueProperty()
                .bindBidirectional(preferencesProperties.getAnimationSpeedProperty());
    }

}
