package org.microcol.gui.util;

import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Region;

/**
 * Horizontal Settings sliders retain native dragging, ticks and keyboard input.
 */
public final class RulerSliderSkin extends SliderSkin {
    private final SettingsControlDrawing drawing = new SettingsControlDrawing();

    private final Region track;

    private final Region thumb;

    public RulerSliderSkin(final Slider control) {
        super(control);
        track = (Region) control.lookup(".track");
        thumb = (Region) control.lookup(".thumb");
        getChildren().add(0, drawing);
        // Native dragging and track-click animation can move the thumb without
        // layout.
        registerChangeListener(thumb.boundsInParentProperty(), v -> draw());
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
    protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
        super.layoutChildren(x, y, w, h);
        draw();
    }

    private void draw() {
        drawing.ruler(getSkinnable(), track, thumb);
    }
}
