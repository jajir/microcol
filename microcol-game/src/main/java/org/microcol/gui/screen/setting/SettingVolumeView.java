package org.microcol.gui.screen.setting;

import org.microcol.gui.MusicPlayer;
import org.microcol.gui.Preferences;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Separate music and effects volume sliders (R-0227). Each shows its caption,
 * the silent and high endpoints and its current value; zero is silent.
 */
@Singleton
public class SettingVolumeView implements JavaFxComponent, UpdatableLanguage {

    public static final String MUSIC_VOLUME_ID = "settingVolume";

    public static final String EFFECTS_VOLUME_ID = "settingEffectsVolume";

    /**
     * Suffix of the label id that shows a slider's current value.
     */
    public static final String VALUE_SUFFIX = "Value";

    /**
     * Suffix of the label id that names a slider's category.
     */
    public static final String CAPTION_SUFFIX = "Caption";

    private final VBox mainPanel;

    private final VolumeRow music;

    private final VolumeRow effects;

    @Inject
    public SettingVolumeView(final I18n i18n) {
        music = new VolumeRow(MUSIC_VOLUME_ID, Preferences.volume_caption);
        effects = new VolumeRow(EFFECTS_VOLUME_ID,
                Preferences.effectsVolume_caption);
        mainPanel = new VBox(4, music.row, effects.row);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        music.updateLanguage(i18n);
        effects.updateLanguage(i18n);
    }

    public DoubleProperty getMusicVolumeValueProperty() {
        return music.slider.valueProperty();
    }

    public DoubleProperty getEffectsVolumeValueProperty() {
        return effects.slider.valueProperty();
    }

    private static final class VolumeRow {

        private final Preferences caption;

        private final HBox row;

        private final Label label;

        private final Slider slider;

        private final Label value;

        private I18n i18n;

        VolumeRow(final String id, final Preferences caption) {
            this.caption = caption;
            label = new Label();
            label.setId(id + CAPTION_SUFFIX);
            label.getStyleClass().add("label-slider");

            slider = new Slider();
            slider.setId(id);
            slider.setMin(MusicPlayer.MIN_VOLUME);
            slider.setMax(MusicPlayer.MAX_VOLUME);
            // Ticks every 1 keep whole values; keys move by 10.
            slider.setMajorTickUnit(10);
            slider.setMinorTickCount(9);
            slider.setSnapToTicks(true);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(false);
            slider.setBlockIncrement(10);

            value = new Label();
            value.setId(id + VALUE_SUFFIX);
            slider.valueProperty()
                    .addListener((object, old, newValue) -> showValue());

            row = new HBox();
            row.getChildren().addAll(label, slider, value);
            HBox.setMargin(value, new Insets(0, 0, 0, 8));
        }

        void updateLanguage(final I18n i18n) {
            this.i18n = i18n;
            label.setText(i18n.get(caption));
            // The skin passes every formatter through one wrapper, so its
            // axis keeps old tick labels unless the formatter is cleared.
            slider.setLabelFormatter(null);
            slider.setLabelFormatter(new StringConverter<Double>() {
                @Override
                public String toString(final Double tick) {
                    if (MusicPlayer.MIN_VOLUME == tick) {
                        return i18n.get(Preferences.volume_silent);
                    }
                    if (MusicPlayer.MAX_VOLUME == tick) {
                        return i18n.get(Preferences.volume_high);
                    }
                    return null;
                }

                @Override
                public Double fromString(final String string) {
                    return null;
                }
            });
            showValue();
        }

        private void showValue() {
            if (i18n == null) {
                return;
            }
            final int volume = (int) slider.getValue();
            value.setText(volume <= MusicPlayer.MIN_VOLUME
                    ? i18n.get(Preferences.volume_silent)
                    : i18n.get(Preferences.volume_value, volume));
        }

    }

}
