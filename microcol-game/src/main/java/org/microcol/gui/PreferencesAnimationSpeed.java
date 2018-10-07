package org.microcol.gui;

import org.microcol.gui.mainmenu.AnimationSpeedChangeController;
import org.microcol.gui.mainmenu.AnimationSpeedChangeEvent;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public final class PreferencesAnimationSpeed extends AbstractMessageWindow
        implements UpdatableLanguage {

    private final GamePreferences gamePreferences;

    private final AnimationSpeedChangeController controller;

    private final StackPane panelSlider;

    private final Label labelCaption;

    private Slider slider;

    /**
     * Constructor when parentFrame is not available.
     * 
     * @param i18n
     *            required localization helper class
     * @param viewUtil
     *            required show dialog utilities
     * @param controller
     *            required animation speed controller
     * @param gamePreferences
     *            required game preferences
     */
    @Inject
    public PreferencesAnimationSpeed(final I18n i18n, final ViewUtil viewUtil,
            final AnimationSpeedChangeController controller,
            final GamePreferences gamePreferences) {
        super(viewUtil, i18n);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.controller = Preconditions.checkNotNull(controller);

        VBox root = new VBox();
        init(root);

        labelCaption = new Label();

        final Button buttonOk = new Button(i18n.get(Loc.ok));
        buttonOk.setOnAction(e -> {
            close();
        });
        buttonOk.requestFocus();

        panelSlider = new StackPane();

        root.getChildren().addAll(labelCaption, panelSlider, buttonOk);
    }

    public void resetAndShowAndWait() {
        showAndWait();
        slider.setValue(gamePreferences.getAnimationSpeed());
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        super.updateLanguage(i18n);
        setTitle(i18n.get(Preferences.animationSpeed_caption));
        labelCaption.setText(i18n.get(Preferences.animationSpeed_caption));
        slider = new Slider();
        slider.setMin(PathPlanning.ANIMATION_SPEED_MIN_VALUE);
        slider.setMax(PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);
        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            /*
             * Every small change is send to listeners. It allows change sound
             * or music immediately.
             */
            controller.fireEvent(new AnimationSpeedChangeEvent(newValue.intValue()));
        });
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double value) {
                if (PathPlanning.ANIMATION_SPEED_MIN_VALUE == value) {
                    return i18n.get(Preferences.animationSpeed_slow);
                }
                if (PathPlanning.ANIMATION_SPEED_MAX_VALUE - 1 == value) {
                    return i18n.get(Preferences.animationSpeed_fast);
                }
                return null;
            }

            @Override
            public Double fromString(final String string) {
                return null;
            }
        });

        panelSlider.getChildren().clear();
        panelSlider.getChildren().add(slider);
    }
}
