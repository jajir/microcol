package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;

import com.google.common.base.MoreObjects;

/**
 * Holds informations about canvas size and position.
 */
class CanvasArea {

    final static Point NOT_READY = Point.of(-1, -1);

    /**
     * It's position of top left corner of canvas on game map.
     */
    private Point topLeft = Point.ZERO;

    /**
     * Define canvas size
     */
    private Point canvasSize = NOT_READY;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(CanvasArea.class).add("topLeft", topLeft)
                .add("canvasSize", canvasSize).toString();
    }

    boolean isReady() {
        return !NOT_READY.equals(canvasSize);
    }

    Point getCanvasSize() {
        return canvasSize;
    }

    void setCanvasWidth(final int newCanvasWidth, final int maxMapWidth) {
        final int x = getNewCanvasPositionWhenSizeChange(newCanvasWidth, canvasSize.getX(),
                topLeft.getX(), maxMapWidth);
        topLeft = Point.of(x, topLeft.getY());
        canvasSize = Point.of(newCanvasWidth, canvasSize.getY());
    }

    void setCanvasHeight(final int newCanvasHeight, final int maxMapHeight) {
        final int y = getNewCanvasPositionWhenSizeChange(newCanvasHeight, canvasSize.getY(),
                topLeft.getY(), maxMapHeight);
        topLeft = Point.of(topLeft.getX(), y);
        canvasSize = Point.of(canvasSize.getX(), newCanvasHeight);
    }

    Point getTopLeft() {
        return topLeft;
    }

    void setTopLeft(final Point topLeft, final Point maxMapSize) {
        this.topLeft = computeNewTopLeftConnerOfCanvas(topLeft, maxMapSize);
    }

    /**
     * Method compute new canvas position when size of canvas change. When
     * canvas size is changed and canvas position will not change than game
     * during screen resizing will be focused on top left corner. Player is
     * focused on screen center. This method compute how canvas position should
     * be changed.
     * <p>
     * Imagine following artificial example. Player see:
     * </p>
     * 
     * <pre>
     *        canvas position     canvas center                               max map size
     *  0          |                     |                                         |
     * -+----------+---------------------*----------------------+------------------+
     *             |                                            |                                             
     *             |                canvas size                 |
     * </pre>
     * 
     * Now player decide to resize screen and canvas position is not recomputed.
     * There will be:
     * 
     * <pre>
     *                           old canvas center                               
     *        canvas position            |                                  max map size
     *  0          |                     |                                        |
     * -+----------+-------------|-------*----------------------------------------+
     *             |             |                                                                           
     *             | canvas size |
     * </pre>
     * 
     * In that case canvas center which was interesting for player is outside of
     * canvas entirely. In that case when canvas size is changed event canvas
     * position should be changed. Like this:
     * 
     * <pre>
     *                              canvas center                               
     *                                     |                                 max map size
     *  0           canvas position |      |                                       |
     * -+---------------------------+------*------+--------------------------------+
     *                              |             |                                                                           
     *                              | canvas size |
     * </pre>
     * 
     * Computing new position of canvas is responsibility of this class.
     * 
     * @param newCanvasSize
     *            new canvas size
     * @param currentCanvasSize
     *            current canvas size
     * @param currentCanvasPosition
     *            current canvas position
     * @param maxMapSize
     *            maximal map size
     * @return
     */
    private int getNewCanvasPositionWhenSizeChange(final int newCanvasSize,
            final int currentCanvasSize, final int currentCanvasPosition, final int maxMapSize) {
        if (newCanvasSize >= maxMapSize) {
            /**
             * Canvas is bigger than map.
             */
            return (maxMapSize - newCanvasSize) / 2;
        } else {
            /**
             * Canvas is smaller than map.
             */
            final int halfOfNewCanvas = newCanvasSize / 2;
            final int center = currentCanvasPosition + currentCanvasSize / 2;
            if (center + halfOfNewCanvas > maxMapSize) {
                return maxMapSize - newCanvasSize;
            }
            if (center - halfOfNewCanvas < 0) {
                return 0;
            }
            return center - halfOfNewCanvas;
        }
    }

    /**
     * Help to scroll screen to some place. It normalize top left screen corner.
     * Prevent screen to scroll outside of visible area.
     *
     * @param newTopLeftCanvasCorner
     *            required position of new top left screen corner
     * @return normalized top left screen corner
     */
    Point computeNewTopLeftConnerOfCanvas(final Point newTopLeftCanvasCorner,
            final Point maxMapSize) {
        return Point.of(
                moveCanvas(newTopLeftCanvasCorner.getX(), canvasSize.getX(), maxMapSize.getX()),
                moveCanvas(newTopLeftCanvasCorner.getY(), canvasSize.getY(), maxMapSize.getY()));
    }

    private int moveCanvas(final int newCanvasPosition, final int canvasSize,
            final int maxMapSize) {
        if (newCanvasPosition < 0) {
            return 0;
        }
        if (newCanvasPosition + canvasSize > maxMapSize) {
            return maxMapSize - canvasSize;
        }
        return newCanvasPosition;
    }

}
