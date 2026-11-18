package org.akropia.gui.screen.setting;

import org.akropia.gui.Preferences;
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
import javafx.util.StringConverter;

@Singleton
final class SettingMapScrollSensitivityView
        implements JavaFxComponent, UpdatableLanguage {
    private final Label label = new Label();
    private final HBox panel;
    private final Slider slider;

    @Inject
    SettingMapScrollSensitivityView(final PreferencesProperties preferences,
            final I18n i18n) {
        slider = new Slider(25, 200,
                preferences.getMapScrollSensitivityProperty().get());
        slider.setId("settingMapScrollSensitivity");
        slider.setBlockIncrement(5);
        slider.setMajorTickUnit(25);
        slider.setMinorTickCount(4);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().bindBidirectional(
                preferences.getMapScrollSensitivityProperty());
        label.setLabelFor(slider);
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
        label.setText(i18n.get(Preferences.mapScrollSensitivity_caption));
        slider.setLabelFormatter(null);
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double tick) {
                if (tick == slider.getMin()) {
                    return i18n.get(Preferences.animationSpeed_slow);
                }
                if (tick == slider.getMax()) {
                    return i18n.get(Preferences.animationSpeed_fast);
                }
                return null;
            }

            @Override
            public Double fromString(final String text) {
                return null;
            }
        });
    }
}
