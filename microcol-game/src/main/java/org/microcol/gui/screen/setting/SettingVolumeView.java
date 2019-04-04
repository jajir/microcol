package org.microcol.gui.screen.setting;

import org.microcol.gui.MusicPlayer;
import org.microcol.gui.Preferences;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

@Singleton
public class SettingVolumeView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final Label label;

    private final Slider slider;

    @Inject
    public SettingVolumeView(final I18n i18n) {
        label = new Label();
        label.getStyleClass().add("label-slider");

        slider = new Slider();
        slider.setMin(MusicPlayer.MIN_VOLUME);
        slider.setMax(MusicPlayer.MAX_VOLUME);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.setBlockIncrement(10);

        mainPanel = new HBox();
        mainPanel.getChildren().add(label);
        mainPanel.getChildren().add(slider);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        label.setText(i18n.get(Preferences.volume_caption));
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double value) {
                if (MusicPlayer.MIN_VOLUME == value) {
                    return i18n.get(Preferences.volume_low);
                }
                if (MusicPlayer.MAX_VOLUME == value) {
                    return i18n.get(Preferences.volume_high);
                }
                return null;
            }

            @Override
            public Double fromString(final String string) {
                return null;
            }
        });
    }

    public DoubleProperty getVolumeValueProperty() {
        return slider.valueProperty();
    }

}
