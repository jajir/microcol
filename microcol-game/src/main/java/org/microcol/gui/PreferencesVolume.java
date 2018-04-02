package org.microcol.gui;

import org.microcol.gui.mainmenu.VolumeChangeController;
import org.microcol.gui.mainmenu.VolumeChangeEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class PreferencesVolume extends AbstractMessageWindow {

    private final GamePreferences gamePreferences;

    private final Slider slider;

    /**
     * Constructor when parentFrame is not available.
     * 
     * @param viewUtil
     *            required tool for centering window on screen
     * @param text
     *            required localization helper class
     * @param volumeChangeController
     *            required volume change controller
     * @param gamePreferences
     *            required game preferences
     */
    @Inject
    public PreferencesVolume(final ViewUtil viewUtil, final Text text,
            final VolumeChangeController volumeChangeController,
            final GamePreferences gamePreferences) {
        super(viewUtil);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        getDialog().setTitle(text.get("preferencesVolume.caption"));

        VBox root = new VBox();
        root.setId("mainVbox");
        init(root);

        final Label label = new Label(text.get("preferencesVolume.caption"));
        label.setId("caption");

        slider = new Slider();
        slider.setMin(MusicPlayer.MIN_VOLUME);
        slider.setMax(MusicPlayer.MAX_VOLUME);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double value) {
                if (MusicPlayer.MIN_VOLUME == value) {
                    return text.get("preferencesVolume.low");
                }
                if (MusicPlayer.MAX_VOLUME == value) {
                    return text.get("preferencesVolume.high");
                }
                return null;
            }

            @Override
            public Double fromString(final String string) {
                return null;
            }
        });
        slider.setBlockIncrement(10);
        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            volumeChangeController.fireEvent(new VolumeChangeEvent(newValue.intValue()));
        });

        final ButtonsBar buttonBar = new ButtonsBar(text);
        buttonBar.getButtonOk().setOnAction(e -> {
            getDialog().close();
        });

        root.getChildren().addAll(label, slider, buttonBar);
    }

    public void resetAndShowAndWait() {
        slider.setValue(gamePreferences.getVolume());
        getDialog().showAndWait();
    }

}
