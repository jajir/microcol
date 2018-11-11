package org.microcol.gui.europe;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.background.AbstractBackground;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@Singleton
public class EuropeBackground extends AbstractBackground {

    @Inject
    public EuropeBackground(final ImageProvider imageProvider) {
        super(imageProvider);
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Point canvas = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getX(), canvas.getY());
    }

}
