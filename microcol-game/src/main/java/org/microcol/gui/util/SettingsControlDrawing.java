package org.microcol.gui.util;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Decoration in logical pixels; native skin nodes retain input and layout. */
final class SettingsControlDrawing extends Canvas {
    private static final Color PAPER = Color.web("#fff9ec");
    private static final Color WOOD = Color.web("#dfbd91");
    private static final Color PRESSED = Color.web("#c69b6b");

    SettingsControlDrawing() {
        setManaged(false);
        setMouseTransparent(true);
        getStyleClass().add("settings-control-drawing");
    }

    private GraphicsContext begin(final Control control) {
        setWidth(control.getWidth());
        setHeight(control.getHeight());
        final GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());
        if (getWidth() < 1 || getHeight() < 1) {
            return null;
        }
        g.setStroke(Color.BLACK);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
        return g;
    }

    private Color fill(final Control control) {
        return control.isDisabled() ? Color.web("#ded5c7")
                : control.isPressed() ? PRESSED
                        : control.isHover() ? Color.web("#efc68f") : WOOD;
    }

    void choice(final Control control, final Region mark, final boolean round,
            final boolean selected, final boolean indeterminate) {
        final GraphicsContext g = begin(control);
        if (g == null) {
            return;
        }
        final Bounds b = mark.getBoundsInParent();
        final double x = b.getMinX(), y = b.getMinY();
        if (round) {
            circle(g, x + 16, y + 16, 15, fill(control), 2);
            circle(g, x + 16, y + 16, 9, PAPER, 1);
            grain(g, x + 10, y + 4, 9);
            if (selected) {
                circle(g, x + 16, y + 16, 5, Color.BLACK, 0);
            }
        } else {
            box(g, x, y, 32, 32, fill(control), 2);
            box(g, x + 6, y + 6, 20, 20, selected ? PRESSED : PAPER, 1);
            grain(g, x + 4, y + 3, 12);
            grain(g, x + 19, y + 29, 9);
            g.setLineWidth(2);
            if (indeterminate) {
                g.strokeLine(x + 10, y + 16, x + 22, y + 16);
            } else if (selected) {
                g.beginPath();
                g.moveTo(x + 8, y + 15);
                g.lineTo(x + 13, y + 21);
                g.lineTo(x + 24, y + 9);
                g.stroke();
            }
        }
        focus(g, control, b);
    }

    void ruler(final Control control, final Region track, final Region thumb) {
        final GraphicsContext g = begin(control);
        if (g == null) {
            return;
        }
        final Bounds rail = track.getBoundsInParent();
        final double x = rail.getMinX(), y = rail.getMinY();
        final double width = rail.getWidth(), height = rail.getHeight();
        box(g, x, y, width, height, WOOD, 2);
        g.setLineWidth(1);
        for (int i = 0; i <= 10; i++) {
            final double tick = x + 3 + i * (width - 6) / 10;
            g.strokeLine(tick, y + height - 3, tick,
                    y + height - (i % 5 == 0 ? 11 : 7));
        }
        final Bounds b = thumb.getBoundsInParent();
        final double cx = (b.getMinX() + b.getMaxX()) / 2;
        final double top = b.getMinY() + 1;
        g.beginPath();
        g.moveTo(cx - 11, top + 1);
        g.quadraticCurveTo(cx, top - 1, cx + 11, top + 1);
        g.lineTo(cx + 10, top + 21);
        g.lineTo(cx, top + 30);
        g.lineTo(cx - 10, top + 21);
        g.closePath();
        g.setFill(fill(control));
        g.fill();
        g.setLineWidth(2);
        g.stroke();
        g.strokeLine(cx, top + 7, cx, top + 23);
        grain(g, cx - 7, top + 4, 13);
        focus(g, control, b);
    }

    private void focus(final GraphicsContext g, final Control control,
            final Bounds b) {
        if (control.isFocused()) {
            g.setLineWidth(1);
            g.setLineDashes(2, 3);
            g.strokeRect(b.getMinX() - 1, b.getMinY() - 1, b.getWidth() + 2,
                    b.getHeight() + 2);
            g.setLineDashes();
        }
    }

    private void box(final GraphicsContext g, final double x, final double y,
            final double w, final double h, final Color color,
            final double stroke) {
        g.beginPath();
        g.moveTo(x + 1, y + 1);
        g.quadraticCurveTo(x + w * .47, y - 1, x + w - 1, y + 1);
        g.quadraticCurveTo(x + w - 2, y + h / 2, x + w - 1, y + h - 2);
        g.quadraticCurveTo(x + w / 2, y + h - 3, x + 1, y + h - 1);
        g.quadraticCurveTo(x + 2, y + h / 2, x + 1, y + 1);
        g.closePath();
        g.setFill(color);
        g.fill();
        g.setLineWidth(stroke);
        g.stroke();
    }

    private void circle(final GraphicsContext g, final double x, final double y,
            final double r, final Color color, final double stroke) {
        g.beginPath();
        g.moveTo(x, y - r);
        g.bezierCurveTo(x + r * .65, y - r * 1.07, x + r * 1.04, y - r * .48,
                x + r, y);
        g.bezierCurveTo(x + r * .99, y + r * .63, x + r * .47, y + r * 1.03, x,
                y + r);
        g.bezierCurveTo(x - r * .65, y + r * .97, x - r * 1.04, y + r * .5,
                x - r, y);
        g.bezierCurveTo(x - r * 1.01, y - r * .65, x - r * .45, y - r * 1.05, x,
                y - r);
        g.closePath();
        g.setFill(color);
        g.fill();
        if (stroke > 0) {
            g.setLineWidth(stroke);
            g.stroke();
        }
    }

    private void grain(final GraphicsContext g, final double x, final double y,
            final double width) {
        g.setLineWidth(1);
        g.beginPath();
        g.moveTo(x, y);
        g.quadraticCurveTo(x + width * .4, y - 1.3, x + width, y);
        g.stroke();
    }
}
