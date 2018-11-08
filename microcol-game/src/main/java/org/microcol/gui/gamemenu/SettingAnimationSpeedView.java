package org.microcol.gui.gamemenu;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.Preferences;
import org.microcol.gui.mainmenu.AnimationSpeedChangeEvent;
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
public class SettingAnimationSpeedView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final Label label;

    private Slider slider;

    private final EventBus eventBus;

    @Inject
    public SettingAnimationSpeedView(final EventBus eventBus,
            final I18n i18n) {
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
        label.setText(i18n.get(Preferences.animationSpeed_caption));
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
            eventBus.post(new AnimationSpeedChangeEvent(newValue.intValue()));
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

        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(label);
        mainPanel.getChildren().add(slider);
    }

}
