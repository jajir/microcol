package org.microcol.gui.screen.statistics;

import org.microcol.gui.Point;
import org.microcol.gui.background.AbstractAnimatedBackground;
import org.microcol.gui.background.RepeatingBackgroundPainter;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class StatisticsBackground extends AbstractAnimatedBackground {

    private final static String IMG_BACKGROUND = "background-wood2.png";

    private final static String IMG_PAPER = "book2.png";

    private final Image imagePaper;

    private final RepeatingBackgroundPainter backgroundPainter;

    @Inject
    public StatisticsBackground(final ImageProvider imageProvider) {
        this.imagePaper = Preconditions.checkNotNull(imageProvider.getImage(IMG_PAPER));
        backgroundPainter = new RepeatingBackgroundPainter(
                Preconditions.checkNotNull(imageProvider.getImage(IMG_BACKGROUND)));
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Point canvasSize = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        if (canvasSize.getX() > 10000 || canvasSize.getY() > 10000) {
            return;
        }
        backgroundPainter.paint(gc, canvasSize);

        final Point centerImageSize = Point.of(imagePaper.getWidth(), imagePaper.getHeight());
        final Point diff = canvasSize.substract(centerImageSize).divide(2);
        gc.drawImage(imagePaper, diff.getX(), diff.getY());

    }

    @Override
    public void beforeHide() {
        super.beforeHide();
    }

    @Override
    public void beforeShow() {
        super.beforeShow();
    }

}
