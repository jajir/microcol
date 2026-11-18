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

@Singleton
final class SettingMapScrollSensitivityView
        implements JavaFxComponent, UpdatableLanguage {
    private final Label label = new Label();
    private final HBox panel;

    @Inject
    SettingMapScrollSensitivityView(final PreferencesProperties preferences,
            final I18n i18n) {
        final Slider slider = new Slider(25, 200,
                preferences.getMapScrollSensitivityProperty().get());
        slider.setId("settingMapScrollSensitivity");
        slider.setBlockIncrement(5);
        slider.setMajorTickUnit(25);
        slider.setMinorTickCount(4);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().bindBidirectional(
                preferences.getMapScrollSensitivityProperty());
        final Label value = new Label();
        value.textProperty().bind(preferences.getMapScrollSensitivityProperty()
                .asString("%d %%"));
        value.setMinWidth(55);
        label.setLabelFor(slider);
        panel = new HBox(label, slider, value);
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
    }
}
