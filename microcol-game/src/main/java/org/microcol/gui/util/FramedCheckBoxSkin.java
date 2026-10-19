package org.microcol.gui.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.skin.CheckBoxSkin;
import javafx.scene.layout.Region;

/** Native checkbox behavior with the selected wooden frame. */
public final class FramedCheckBoxSkin extends CheckBoxSkin {
    private final SettingsControlDrawing drawing = new SettingsControlDrawing();

    public FramedCheckBoxSkin(final CheckBox control) {
        super(control);
        getChildren().add(0, drawing);
        registerChangeListener(control.selectedProperty(),
                v -> control.requestLayout());
        registerChangeListener(control.indeterminateProperty(),
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
        final CheckBox control = getSkinnable();
        drawing.choice(control, (Region) control.lookup(".box"), false,
                control.isSelected(), control.isIndeterminate());
    }
}
