package org.microcol.gui;

import org.microcol.gui.mainmenu.AnimationSpeedChangeController;
import org.microcol.gui.mainmenu.AnimationSpeedChangeEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public final class PreferencesAnimationSpeed extends AbstractMessageWindow {

    private final GamePreferences gamePreferences;

    private final Slider slider;

    /**
     * Constructor when parentFrame is not available.
     * 
     * @param text
     *            required localization helper class
     * @param viewUtil
     *            required show dialog utilities
     * @param controller
     *            required animation speed controller
     * @param gamePreferences
     *            required game preferences
     */
    @Inject
    public PreferencesAnimationSpeed(final Text text, final ViewUtil viewUtil,
            final AnimationSpeedChangeController controller,
            final GamePreferences gamePreferences) {
        super(viewUtil);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        setTitle(text.get("preferencesAnimationSpeed.caption"));

        VBox root = new VBox();
        init(root);

        final Label label = new Label(text.get("preferencesAnimationSpeed.caption"));

        slider = new Slider();
        slider.setMin(PathPlanning.ANIMATION_SPEED_MIN_VALUE);
        slider.setMax(PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double value) {
                if (PathPlanning.ANIMATION_SPEED_MIN_VALUE == value) {
                    return text.get("preferencesAnimationSpeed.slow");
                }
                if (PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1 == value) {
                    return text.get("preferencesAnimationSpeed.fast");
                }
                return null;
            }

            @Override
            public Double fromString(final String string) {
                return null;
            }
        });
        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            /*
             * Every small change is send to listeners. It allows change sound
             * or music immediately.
             */
            controller.fireEvent(new AnimationSpeedChangeEvent(newValue.intValue()));
        });

        final Button buttonOk = new Button(text.get("dialog.ok"));
        buttonOk.setOnAction(e -> {
            close();
        });
        buttonOk.requestFocus();

        root.getChildren().addAll(label, slider, buttonOk);
    }

    public void resetAndShowAndWait() {
        slider.setValue(gamePreferences.getAnimationSpeed());
        showAndWait();
    }

}
