package org.akropia.gui.screen.setting;

import org.akropia.gui.Preferences;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.preferences.PreferencesProperties;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;
import org.akropia.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
final class SettingMapScrollSpeedView
        implements JavaFxComponent, UpdatableLanguage {
    private final Label label = new Label();
    private final HBox panel;

    @Inject
    SettingMapScrollSpeedView(final PreferencesProperties preferences,
            final I18n i18n) {
        label.getStyleClass().add("label-slider");
        final Slider slider = new Slider(1,
                GamePreferences.MAX_MAP_SCROLL_SPEED,
                preferences.getMapScrollSpeedProperty().get());
        slider.setId("settingMapScrollSpeed");
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(true);
        slider.valueProperty()
                .bindBidirectional(preferences.getMapScrollSpeedProperty());
        panel = new HBox(label, slider);
        SettingRow.align(panel, label);
        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return panel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        label.setText(i18n.get(Preferences.mapScrollSpeed_caption));
    }
}
