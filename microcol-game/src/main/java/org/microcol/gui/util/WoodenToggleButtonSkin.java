package org.microcol.gui.util;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.ToggleButtonSkin;

/** Standard toggle behavior with the shared wooden button decoration. */
public final class WoodenToggleButtonSkin extends ToggleButtonSkin {
    private final WoodenButtonDrawing drawing = new WoodenButtonDrawing();

    public WoodenToggleButtonSkin(final ToggleButton button) {
        super(button);
        button.getStyleClass().add("wooden-button");
        getChildren().add(0, drawing);
        registerChangeListener(button.hoverProperty(),
                value -> button.requestLayout());
        registerChangeListener(button.armedProperty(),
                value -> button.requestLayout());
        registerChangeListener(button.focusedProperty(),
                value -> button.requestLayout());
        registerChangeListener(button.disabledProperty(),
                value -> button.requestLayout());
        registerChangeListener(button.selectedProperty(),
                value -> button.requestLayout());
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
        super.layoutChildren(x, y + (getSkinnable().isArmed() ? 2 : 0), w, h);
        drawing.draw(getSkinnable());
    }

    @Override
    public void dispose() {
        getSkinnable().getStyleClass().remove("wooden-button");
        super.dispose();
    }
}
