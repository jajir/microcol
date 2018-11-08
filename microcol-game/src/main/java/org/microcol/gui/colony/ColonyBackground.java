package org.microcol.gui.colony;

import org.microcol.gui.image.ImageProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;

@Singleton
public class ColonyBackground extends BackgroundImage {

    @Inject
    public ColonyBackground(final ImageProvider imageProvider) {
        super(imageProvider, "colony.png");
    }

    @Override
    public void paint(final GraphicsContext gc) {
        super.paint(gc);
    }

}
