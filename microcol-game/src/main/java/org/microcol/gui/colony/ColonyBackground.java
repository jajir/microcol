package org.microcol.gui.colony;

import org.microcol.gui.Point;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.AbstractBackgroundPanel;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@Singleton
public class ColonyBackground extends AbstractBackgroundPanel {

    @Inject
    public ColonyBackground(final ImageProvider imageProvider) {
        super(imageProvider);
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Point canvas = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        if (canvas.getX() > 10000 || canvas.getY() > 10000) {
            return;
        }
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getX(), canvas.getY());
    }

}
