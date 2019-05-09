package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Listener;
import org.microcol.model.Location;
import org.microcol.model.event.GameStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Paint clouds. It draws cloud coming from 45 degree angle to point [0,0]. When
 * cloud went out from map than process repeat again.
 */
@Listener
public class AnimationClouds {

    private final static double MOVE_STEP = 0.5D;
    private final VisibleAreaService visibleAreaService;
    private final Image cloudImage;
    private final double angle = Math.toRadians(45);
    private double position;

    @Inject
    AnimationClouds(final VisibleAreaService visibleAreaService,
            final ImageProvider imageProvider) {
        this.visibleAreaService = Preconditions.checkNotNull(visibleAreaService);
        cloudImage = imageProvider.getImage("cloud-1.png");
    }

    @Subscribe
    private void onGameStarted(final GameStartedEvent event) {
        final Location maxMap = event.getModel().getMap().getMaxLocation();
        final Point p = Tile.ofLocation(maxMap).getTopLeftCorner();
        position = initPosition(p);
    }

    void paint(final GraphicsContext g) {
        position -= MOVE_STEP;
        final Point cloudTopLeftCorner = Point.of(Math.cos(angle) * position,
                Math.sin(angle) * position);
        if (isCloudOnMap(cloudTopLeftCorner)) {
            if (isCloudVisible(cloudTopLeftCorner)) {
                Point point = cloudTopLeftCorner.substract(visibleAreaService.getTopLeft());
                g.drawImage(cloudImage, point.getX(), point.getY());
            }
        } else {
            final Point p = visibleAreaService.getMapSize();
            position = initPosition(p);
        }
    }

    private double initPosition(final Point mapSize) {
        return mapSize.getY() / Math.sin(angle);
    }

    /**
     * Verify that image in on map.
     * 
     * @param cloudTopLeftCorner
     *            required top left of cloud.
     * @return Return <code>true</code> when at least one corner is at map
     *         otherwise return <code>false</code>.
     */
    private boolean isCloudOnMap(final Point cloudTopLeftCorner) {
        final int width = (int) cloudImage.getWidth();
        final int height = (int) cloudImage.getHeight();
        if (visibleAreaService.isMapPointValid(cloudTopLeftCorner)) {
            return true;
        }
        if (visibleAreaService.isMapPointValid(cloudTopLeftCorner.add(width, 0))) {
            return true;
        }
        if (visibleAreaService.isMapPointValid(cloudTopLeftCorner.add(0, height))) {
            return true;
        }
        if (visibleAreaService.isMapPointValid(cloudTopLeftCorner.add(width, height))) {
            return true;
        }
        return false;
    }

    /**
     * Verify that image is visible on canvas.
     *
     * @param cloudTopLeftCorner
     *            required top left corner of canvas
     * @return <true> when cloud is visible on canvas otherwise return
     *         <code>false</code>
     */
    private boolean isCloudVisible(final Point cloudTopLeftCorner) {
        final int width = (int) cloudImage.getWidth();
        final int height = (int) cloudImage.getHeight();
        if (visibleAreaService.isVisibleMapPoint(cloudTopLeftCorner)) {
            return true;
        }
        if (visibleAreaService.isVisibleMapPoint(cloudTopLeftCorner.add(width, 0))) {
            return true;
        }
        if (visibleAreaService.isVisibleMapPoint(cloudTopLeftCorner.add(0, height))) {
            return true;
        }
        if (visibleAreaService.isVisibleMapPoint(cloudTopLeftCorner.add(width, height))) {
            return true;
        }
        return false;
    }

}
