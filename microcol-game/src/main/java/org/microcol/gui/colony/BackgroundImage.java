package org.microcol.gui.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractBackgroundPanel;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class BackgroundImage extends AbstractBackgroundPanel {

    private final String imageName;

    public BackgroundImage(ImageProvider imageProvider, final String imageName) {
        super(imageProvider);
        this.imageName = Preconditions.checkNotNull(imageName);
    }

    @Override
    public void paint(final GraphicsContext gc) {
        super.paint(gc);
        final Point canvas = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        if (canvas.getX() > 10000 || canvas.getY() > 10000) {
            return;
        }
        final Image image = getImageProvider().getImage(imageName);
        final Point center = canvas.divide(2);
        final Point imageSize = Point.of(image.getWidth(), image.getHeight()).divide(2);
        final Point p = center.substract(imageSize);
        gc.drawImage(image, p.getX(), p.getY());
    }

}
