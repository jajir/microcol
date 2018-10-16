package org.microcol.gui.gamemenu;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractBackgroundPanel;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Repaintable;

import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Contains background image.
 */
public class BackgroundPanel extends AbstractBackgroundPanel
        implements JavaFxComponent, Repaintable {

    @Inject
    public BackgroundPanel(final ImageProvider imageProvider) {
        super(imageProvider);
        repaint();
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Image image = getImageProvider().getImage(ImageProvider.IMG_SUNSET);
        final Point center = Point.of(getCanvas().getWidth(), getCanvas().getHeight()).divide(2);
        final Point imageSize = Point.of(image.getWidth(), image.getHeight()).divide(2);
        final Point p = center.substract(imageSize);
        gc.drawImage(image, p.getX(), p.getY());
    }

}
