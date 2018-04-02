package org.microcol.gui.gamepanel;

import java.util.Optional;
import java.util.function.Consumer;

import org.microcol.gui.MainGamePanelView;
import org.microcol.gui.Point;
import org.microcol.gui.util.OneTimeConditionalEvent;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * User see some area where is game map drawn this area is called canvas. Class
 * define relation between game map, canvas size and canvas position on map.Game
 * map could be smaller than canvas also could be bigger.
 * 
 * <p>
 * This class work just with {@link Point} class.
 * </p>
 * 
 */
public class VisibleArea {

    /**
     * It's position of top left corner of canvas on game map.
     */
    private Point topLeft = Point.of(0, 0);

    /**
     * Define canvas width.
     */
    private int canvasWidth;

    /**
     * Define canvas height.
     */
    private int canvasHeight;

    /**
     * Define map size.
     */
    private Optional<Point> maxMapSize = Optional.empty();

    /**
     * Extra one time listener called when canvas height is set.
     */
    private final OneTimeConditionalEvent<String> onVisibleAreaIsReady = new OneTimeConditionalEvent<>();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(VisibleArea.class).add("topLeft", topLeft)
                .add("canvasWidth", canvasWidth).add("canvasHeight", canvasHeight)
                .add("maxMapSize", maxMapSize).toString();
    }

    public void setMaxMapSize(final WorldMap worldMap) {
        Preconditions.checkNotNull(worldMap);
        maxMapSize = Optional.of(Point.of(Location.of(worldMap.getMaxX(), worldMap.getMaxY())));
        topLeft = Point.of(0, 0);
        /**
         * Following code force class to compute correct position of top left
         * corner of map.
         */
        setCanvasHeight(canvasHeight);
        setCanvasWidth(canvasWidth);
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasWidth(final int newCanvasWidth) {
        if (maxMapSize.isPresent()) {
            if (newCanvasWidth > maxMapSize.get().getX()) {
                /**
                 * Visible area is greater than map. Map should be centered.
                 */
                final int x = -(newCanvasWidth - maxMapSize.get().getX()) / 2;
                topLeft = Point.of(x, topLeft.getY());
            } else {
                /**
                 * whole map can't fit canvas
                 */
                final int toGrow = maxMapSize.get().getX() - canvasWidth;
                final int toGrowLeft = topLeft.getX();
                final int deltaGrow = newCanvasWidth - canvasWidth;
                final int x = (int) (deltaGrow * ((float) toGrowLeft / toGrow));
                topLeft = Point.of(topLeft.getX() - x, topLeft.getY());
            }
        }
        this.canvasWidth = newCanvasWidth;
        onVisibleAreaIsReady.condition1Passed();
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasHeight(final int newCanvasHeight) {
        if (maxMapSize.isPresent()) {
            if (newCanvasHeight > maxMapSize.get().getY()) {
                /**
                 * Visible area is greater than map. Map should be centered.
                 */
                final int y = -(newCanvasHeight - maxMapSize.get().getY()) / 2;
                topLeft = Point.of(topLeft.getX(), y);
            } else {
                /**
                 * whole map can't fit canvas
                 */
                final int toGrow = maxMapSize.get().getY() - canvasHeight;
                final int toGrowLeft = topLeft.getY();
                final int deltaGrow = newCanvasHeight - canvasHeight;
                final int y = (int) (deltaGrow * ((float) toGrowLeft / toGrow));
                topLeft = Point.of(topLeft.getX(), topLeft.getY() - y);
            }
        }
        this.canvasHeight = newCanvasHeight;
        onVisibleAreaIsReady.condition2Passed();
    }

    public void setX(int x) {
        topLeft = Point.of(x, topLeft.getY());
    }

    public void setY(int y) {
        topLeft = Point.of(topLeft.getX(), y);
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public Point getBottomRight() {
        return topLeft.add(canvasWidth, canvasHeight);
    }

    public void addDeltaToTopLeftPoint(final Point delta) {
        int x = topLeft.getX();
        int y = topLeft.getY();

        if (canvasWidth < maxMapSize.get().getX()) {
            /**
             * X could be adjusted
             */
            x = adjust(delta.getX(), topLeft.getX(), maxMapSize.get().getX(), canvasWidth);
        }

        if (canvasHeight < maxMapSize.get().getY()) {
            /**
             * Y could be adjusted
             */
            y = adjust(delta.getY(), topLeft.getY(), maxMapSize.get().getY(), canvasHeight);
        }

        topLeft = Point.of(x, y);
    }

    private int adjust(final int delta, final int original, final int maxMap, final int canvasMax) {
        final int adjusted = original + delta;
        if (adjusted < 0) {
            return 0;
        }
        if (adjusted > maxMap) {
            return maxMap;
        }
        if (adjusted + canvasMax > maxMap) {
            return original;
        }
        return adjusted;
    }

    private int adjustToLess(final int delta, final int original, final int maxMap,
            final int canvasMax) {
        if (canvasMax > maxMap) {
            return original;
        }
        final int adjusted = original + delta;
        if (adjusted < 0) {
            return 0;
        }
        if (adjusted > maxMap) {
            return maxMap;
        }
        if (adjusted + canvasMax > maxMap) {
            return maxMap - canvasMax;
        }
        return adjusted;
    }

    /**
     * Help to scroll screen to some place. It normalize top left screen corner.
     * Prevent screen to scroll outside of visible area.
     * 
     * @param newTopLeftScreenCorner
     *            required position of new top left screen corner
     * @return normalized top left screen corner
     */
    public Point scrollToPoint(final Point newTopLeftScreenCorner) {
        final Point delta = newTopLeftScreenCorner.substract(topLeft);
        if (maxMapSize.isPresent()) {
            return Point.of(
                    adjustToLess(delta.getX(), topLeft.getX(), maxMapSize.get().getX(),
                            canvasWidth),
                    adjustToLess(delta.getY(), topLeft.getY(), maxMapSize.get().getY(),
                            canvasHeight));
        } else {
            return Point.of(
                    adjustToLess(delta.getX(), topLeft.getX(),
                            MainGamePanelView.MAX_CANVAS_SIDE_LENGTH, canvasWidth),
                    adjustToLess(delta.getY(), topLeft.getY(),
                            MainGamePanelView.MAX_CANVAS_SIDE_LENGTH, canvasHeight));
        }
    }

    /**
     * @param onCanvasReady
     *            the onCanvasReady to set
     */
    public void setOnCanvasReady(final Consumer<String> onCanvasReady) {
        onVisibleAreaIsReady.setOnConditionsPassed(onCanvasReady);
    }

}
