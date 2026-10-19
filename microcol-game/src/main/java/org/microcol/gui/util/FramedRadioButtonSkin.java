package org.microcol.gui.util;

import javafx.scene.control.RadioButton;
import javafx.scene.control.skin.RadioButtonSkin;
import javafx.scene.layout.Region;

/** Native mutually exclusive choices with the selected wooden frame. */
public final class FramedRadioButtonSkin extends RadioButtonSkin {
    private final SettingsControlDrawing drawing = new SettingsControlDrawing();

    public FramedRadioButtonSkin(final RadioButton control) {
        super(control);
        getChildren().add(0, drawing);
        registerChangeListener(control.selectedProperty(),
                v -> control.requestLayout());
        registerChangeListener(control.hoverProperty(),
                v -> control.requestLayout());
        registerChangeListener(control.pressedProperty(),
                v -> control.requestLayout());
        registerChangeListener(control.focusedProperty(),
                v -> control.requestLayout());
        registerChangeListener(control.disabledProperty(),
                v -> control.requestLayout());
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        if (drawing != null) {
            getChildren().add(0, drawing);
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        final RadioButton control = getSkinnable();
        drawing.choice(control, (Region) control.lookup(".radio"), true,
                control.isSelected(), false);
    }
}
