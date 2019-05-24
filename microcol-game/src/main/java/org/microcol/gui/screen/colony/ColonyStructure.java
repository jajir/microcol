package org.microcol.gui.screen.colony;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

class ColonyStructure {

    private final Point leftTopCorner;

    private final Point size;

    private boolean isMouseInside;

    private EventHandler<? super MouseEvent> onMouseEntered;

    private EventHandler<? super MouseEvent> onMouseExited;

    ColonyStructure(final Point leftTopCorner, final Point size) {
        this.leftTopCorner = Preconditions.checkNotNull(leftTopCorner);
        this.size = Preconditions.checkNotNull(size);
        isMouseInside = false;
        onMouseEntered = null;
        onMouseExited = null;
    }

    void setOnMouseEntered(final EventHandler<? super MouseEvent> onMouseEntered) {
        this.onMouseEntered = Preconditions.checkNotNull(onMouseEntered);
    }

    void setOnMouseExited(final EventHandler<? super MouseEvent> onMouseExited) {
        this.onMouseExited = Preconditions.checkNotNull(onMouseExited);
    }

    void evaluateMouseMove(final MouseEvent event) {
        if (onMouseEntered != null) {
            final Rectangle rectangle = Rectangle.ofPointAndSize(leftTopCorner, size);
            final Point point = Point.of(event.getX(), event.getY());
            if (rectangle.isIn(point)) {
                if (!isMouseInside) {
                    isMouseInside = true;
                    onMouseEntered.handle(event);
                }
            } else {
                if (isMouseInside) {
                    isMouseInside = false;
                    onMouseExited.handle(event);
                }
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ColonyStructure.class).add("leftTopCorner", leftTopCorner)
                .add("size", size).toString();
    }

}
