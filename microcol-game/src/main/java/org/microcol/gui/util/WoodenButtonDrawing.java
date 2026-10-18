package org.microcol.gui.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Hand-drawn decoration at the control's actual logical size. */
final class WoodenButtonDrawing extends Canvas {

    WoodenButtonDrawing() {
        setManaged(false);
        setMouseTransparent(true);
        getStyleClass().add("wooden-button-drawing");
    }

    void draw(final ButtonBase button) {
        final double w = button.getWidth();
        final double h = button.getHeight();
        setWidth(w);
        setHeight(h);
        final GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        if (w < 20 || h < 20) {
            return;
        }
        final boolean selected = button instanceof ToggleButton
                && ((ToggleButton) button).isSelected();
        final boolean pressed = button.isArmed();
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
        g.setStroke(Color.BLACK);
        if (!pressed) {
            g.save();
            g.translate(0, 3);
            outline(g, w, h);
            g.setFill(Color.web("#fff9ec"));
            g.fill();
            g.setLineWidth(1.5);
            g.stroke();
            g.restore();
        }
        g.save();
        g.translate(0, pressed ? 2 : 0);
        outline(g, w, h);
        g.setFill(Color.web("#fff9ec"));
        g.fill();
        g.setLineWidth(2);
        g.stroke();
        g.setFill(Color.web(button.isDisabled() ? "#ded5c7"
                : pressed || selected ? "#c69b6b"
                        : button.isHover() ? "#efc68f" : "#dfbd91"));
        g.beginPath();
        g.moveTo(7, 8);
        g.quadraticCurveTo(w / 2, 6, w - 7, 8);
        g.lineTo(w - 8, h - 11);
        g.quadraticCurveTo(w / 2, h - 13, 7, h - 10);
        g.closePath();
        g.fill();
        g.setLineWidth(1);
        final double length = Math.min(40, (w - 24) / 3);
        g.beginPath();
        g.moveTo(11, 10);
        g.quadraticCurveTo(11 + length / 2, 8, 11 + length, 10);
        g.moveTo(w - 11 - length, h - 13);
        g.quadraticCurveTo(w - 11 - length / 2, h - 10, w - 11, h - 12);
        g.stroke();
        if (w > 150) {
            g.beginPath();
            g.moveTo(w - 53, 10);
            g.quadraticCurveTo(w - 30, 8, w - 12, 11);
            g.moveTo(12, h - 13);
            g.quadraticCurveTo(30, h - 10, 51, h - 12);
            g.stroke();
        }
        if (selected) {
            g.setLineWidth(2);
            g.strokeLine(w / 2 - 9, h - 10, w / 2 + 9, h - 10);
        }
        g.restore();
        if (button.isFocused()) {
            g.setLineWidth(1);
            g.setLineDashes(2, 3);
            g.strokeRect(1, 1, w - 2, h - 2);
            g.setLineDashes();
        }
    }

    private void outline(final GraphicsContext g, final double w,
            final double h) {
        g.beginPath();
        g.moveTo(4, 4);
        g.quadraticCurveTo(w / 2, 1, w - 4, 4);
        g.quadraticCurveTo(w - 5, h / 2, w - 4, h - 7);
        g.quadraticCurveTo(w / 2, h - 9, 4, h - 6);
        g.quadraticCurveTo(6, h / 2, 4, 4);
        g.closePath();
    }
}
