package org.microcol.gui.gamemenu;

import org.microcol.gui.MusicPlayer;
import org.microcol.gui.Preferences;
import org.microcol.gui.mainmenu.VolumeChangeEvent;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

@Singleton
public class SettingVolumeView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final Label label;

    private Slider slider;

    private final EventBus eventBus;

    @Inject
    public SettingVolumeView(final EventBus eventBus, final I18n i18n) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        label = new Label();
        label.getStyleClass().add("label-slider");
        
        mainPanel = new HBox();

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        label.setText(i18n.get(Preferences.volume_caption));
        slider = new Slider();
        slider.setMin(MusicPlayer.MIN_VOLUME);
        slider.setMax(MusicPlayer.MAX_VOLUME);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.setBlockIncrement(10);
        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            eventBus.post(new VolumeChangeEvent(newValue.intValue()));
        });
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

        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(label);
        mainPanel.getChildren().add(slider);
    }

}
