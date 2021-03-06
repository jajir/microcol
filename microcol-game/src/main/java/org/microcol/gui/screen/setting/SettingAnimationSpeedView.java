package org.microcol.gui.screen.setting;

import org.microcol.gui.Preferences;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.PathPlanningService;
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
class SettingAnimationSpeedView implements JavaFxComponent, UpdatableLanguage {

    private final HBox mainPanel;

    private final Label label;

    private final Slider slider;

    @Inject
    SettingAnimationSpeedView(final I18n i18n) {
        label = new Label();
        label.getStyleClass().add("label-slider");

        slider = new Slider();
        slider.setMin(PathPlanningService.ANIMATION_SPEED_MIN_VALUE);
        slider.setMax(PathPlanningService.ANIMATION_SPEED_MAX_VALUE - 1);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);

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
    public void updateLanguage(final I18n i18n) {
        label.setText(i18n.get(Preferences.animationSpeed_caption));
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(final Double value) {
                if (PathPlanningService.ANIMATION_SPEED_MIN_VALUE == value) {
                    return i18n.get(Preferences.animationSpeed_slow);
                }
                if (PathPlanningService.ANIMATION_SPEED_MAX_VALUE - 1 == value) {
                    return i18n.get(Preferences.animationSpeed_fast);
                }
                return null;
            }

            @Override
            public Double fromString(final String string) {
                return null;
            }
        });
    }

    public DoubleProperty getAnimationSpeedValueProperty() {
        return slider.valueProperty();
    }

}
