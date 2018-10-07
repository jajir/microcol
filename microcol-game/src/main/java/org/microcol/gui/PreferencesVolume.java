package org.microcol.gui;

import org.microcol.gui.mainmenu.VolumeChangeController;
import org.microcol.gui.mainmenu.VolumeChangeEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public final class PreferencesVolume extends AbstractMessageWindow implements UpdatableLanguage {

    private final GamePreferences gamePreferences;

    private final VolumeChangeController volumeChangeController;

    private final StackPane panelSlider;

    private Slider slider;

    private final Label labelCaption;

    /**
     * Constructor when parentFrame is not available.
     * 
     * @param viewUtil
     *            required tool for centering window on screen
     * @param i18n
     *            required localization helper class
     * @param volumeChangeController
     *            required volume change controller
     * @param gamePreferences
     *            required game preferences
     */
    @Inject
    public PreferencesVolume(final ViewUtil viewUtil, final I18n i18n,
            final VolumeChangeController volumeChangeController,
            final GamePreferences gamePreferences) {
        super(viewUtil, i18n);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.volumeChangeController = Preconditions.checkNotNull(volumeChangeController);

        VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        labelCaption = new Label();
        labelCaption.setId("caption");

        final ButtonsBar buttonBar = new ButtonsBar(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        panelSlider = new StackPane();

        root.getChildren().addAll(labelCaption, panelSlider, buttonBar);
    }

    public void resetAndShowAndWait() {
        showAndWait();
        slider.setValue(gamePreferences.getVolume());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        super.updateLanguage(i18n);
        setTitle(i18n.get(Preferences.volume_caption));
        labelCaption.setText(i18n.get(Preferences.volume_caption));
        panelSlider.getChildren().clear();
        slider = new Slider();
        slider.setMin(MusicPlayer.MIN_VOLUME);
        slider.setMax(MusicPlayer.MAX_VOLUME);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.setBlockIncrement(10);
        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            volumeChangeController.fireEvent(new VolumeChangeEvent(newValue.intValue()));
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
        panelSlider.getChildren().add(slider);
    };

}
