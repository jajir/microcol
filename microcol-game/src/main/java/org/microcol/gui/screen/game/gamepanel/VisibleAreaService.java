package org.microcol.gui.screen.game.gamepanel;

import java.util.function.Consumer;

import org.microcol.gui.Point;
import org.microcol.gui.Rectangle;
import org.microcol.gui.Tile;
import org.microcol.gui.util.OneTimeConditionalEvent;
import org.microcol.model.Location;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

/**
 * User see some area where is game map drawn this area is called canvas. Class
 * define relation between game map, canvas size and canvas position on map.Game
 * map could be smaller than canvas also could be bigger.
 * <p>
 * Following examples shows case when bigger map with smaller canvas
 * </p>
 * 
 * <pre>
 * +------------------------------------------------------+
 * |                                                      |
 * |                 [top left]                           |
 * |                    +------------+                    |
 * |                    |            |                    |
 * |                    |   canvas   | [height]           |
 * |                    |            |                    |
 * |                    |            |                    |
 * |                    +------------+                    |
 * |                        [width]                       |
 * |                                                      |
 * |                                                      |
 * +------------------------------------------------------+
 * </pre>
 * <p>
 * All variables should be immediately set when they are changed on player's
 * monitor. Usually by some property listener.
 * </p>
 * <p>
 * This class work just with {@link Point} class.
 * </p>
 * 
 */
@Singleton
class VisibleAreaService {

    /**
     * Maximal reasonable canvas size.
     */
    final static int MAX_CANVAS_SIDE_LENGTH = 10000;

    /**
     * It helps consider if canvas size is reasonable. When canvas side length
     * is bigger than this it's not correct size.
     */
    final static Point MAX_CANVAS_SIZE = Point.of(MAX_CANVAS_SIDE_LENGTH, MAX_CANVAS_SIDE_LENGTH);

    /**
     * Holds information about canvas size and position relative to world map in
     * points.
     */
    private final CanvasArea canvasArea = new CanvasArea();

    /**
     * Define map size.
     */
    private Point maxMapSize = null;

    /**
     * Extra one time listener called when canvas height is set.
     */
    private final OneTimeConditionalEvent<String> onVisibleAreaIsReady = new OneTimeConditionalEvent<>();

    private int getCanvasWidth() {
        return canvasArea.getCanvasSize().getX();
    }

    private int getCanvasHeight() {
        return canvasArea.getCanvasSize().getY();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(VisibleAreaService.class).add("canvasArea", canvasArea)
                .add("maxMapSize", maxMapSize).toString();
    }

    public void setWorldMap(final Location worldMaxLocation) {
        Preconditions.checkNotNull(worldMaxLocation);
        maxMapSize = Tile.ofLocation(worldMaxLocation).getBottomRightCorner();
        canvasArea.setTopLeft(Point.ZERO, MAX_CANVAS_SIZE);
        /**
         * Following code force class to compute correct position of top left
         * corner of map.
         */
        if (isReady()) {
            setCanvasHeight(getCanvasHeight());
            setCanvasWidth(getCanvasWidth());
        }
    }

    public Point getCanvasSize() {
        return canvasArea.getCanvasSize();
    }

    public void setCanvasWidth(final int newCanvasWidth) {
        canvasArea.setCanvasWidth(newCanvasWidth, getMapSize().getX());
        onVisibleAreaIsReady.setCondition1Passed();
    }

    public void setCanvasHeight(final int newCanvasHeight) {
        canvasArea.setCanvasHeight(newCanvasHeight, getMapSize().getY());
        onVisibleAreaIsReady.setCondition2Passed();
    }

    public boolean isReady() {
        return canvasArea.isReady();
    }

    public void setTopLeftPosionOfCanvas(final Point newTopLeft) {
        canvasArea.setTopLeft(newTopLeft, getMapSize());
    }

    /**
     * Provide position of canvas relative to map coordinates in points.
     * 
     * @return canvas top left corner position
     */
    public Point getTopLeft() {
        return canvasArea.getTopLeft();
    }

    public Point getBottomRight() {
        return getTopLeft().add(getCanvasSize());
    }

    /**
     * Help to scroll screen to some place. It normalize top left screen corner.
     * Prevent screen to scroll outside of visible area.
     *
     * @param newTopLeftCanvasCorner
     *            required position of new top left screen corner
     * @return normalized top left screen corner
     */
    Point computeNewTopLeftConnerOfCanvas(final Point newTopLeftCanvasCorner) {
        return canvasArea.computeNewTopLeftConnerOfCanvas(newTopLeftCanvasCorner, getMapSize());
    }

    void addDeltaToTopLeftPoint(final Point delta) {
        final Point newTopLeft = canvasArea.getTopLeft().add(delta);
        setTopLeftPosionOfCanvas(newTopLeft);
    }

    /**
     * @param onCanvasReady
     *            the onCanvasReady to set
     */
    public void setOnCanvasReady(final Consumer<String> onCanvasReady) {
        onVisibleAreaIsReady.setOnConditionsPassed(onCanvasReady);
    }

    Point getMapSize() {
        if (maxMapSize == null) {
            return MAX_CANVAS_SIZE;
        }
        return maxMapSize;
    }

    /**
     * Verify that given point is in canvas. Point is in map coordinates.
     * 
     * @param point
     *            required point in map coordinates
     * @return return <code>true</code> when point is inside area otherwise
     *         return <code>false</code>
     */
    boolean isVisibleMapPoint(final Point point) {
        final Rectangle canvas = Rectangle.ofPointAndSize(canvasArea.getTopLeft(),
                canvasArea.getCanvasSize());
        return canvas.isIn(point);
    }

    /**
     * Verify that given canvas point is visible.
     * 
     * @param point
     *            required point in canvas (on screen) coordinates
     * @return return <code>true</code> when point is inside area otherwise
     *         return <code>false</code>
     */
    boolean isVisibleCanvasPoint(final Point point) {
        final Rectangle canvas = Rectangle.ofPointAndSize(Point.ZERO, canvasArea.getCanvasSize());
        return canvas.isIn(point);
    }

    /**
     * Verify that given point is valid point on map.
     *
     * @param point
     *            required point
     * @return return <code>true</code> when point is on map otherwise return
     *         <code>false</code>.
     */
    boolean isMapPointValid(final Point point) {
        final Rectangle canvas = Rectangle.ofPointAndSize(Point.ZERO, getMapSize());
        return canvas.isIn(point);
    }

}
