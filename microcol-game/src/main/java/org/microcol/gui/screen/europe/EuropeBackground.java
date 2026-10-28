package org.microcol.gui.screen.europe;

import org.microcol.gui.background.AbstractBackground;
import org.microcol.gui.image.ImageProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

@Singleton
public class EuropeBackground extends AbstractBackground {
    static final double QUAY_HEIGHT = 112;
    private final Image image;

    @Inject
    public EuropeBackground(final ImageProvider imageProvider) {
        image = imageProvider.getImage("europe-harbor.png");
        getContent().setMouseTransparent(true);
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final double width = getCanvas().getWidth();
        final double height = getCanvas().getHeight();
        if (width <= 0 || height <= QUAY_HEIGHT) {
            return;
        }
        // Keep the cargo recess tall enough while the sea absorbs resizing.
        final double front = image.getHeight() * 0.825;
        gc.drawImage(image, 0, 0, image.getWidth(), front, 0, 0, width,
                height - QUAY_HEIGHT);
        gc.drawImage(image, 0, front, image.getWidth(),
                image.getHeight() - front, 0, height - QUAY_HEIGHT, width,
                QUAY_HEIGHT);
    }
}
