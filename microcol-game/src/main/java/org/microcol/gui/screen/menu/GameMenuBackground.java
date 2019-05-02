package org.microcol.gui.screen.menu;

import org.microcol.gui.GuiColors;
import org.microcol.gui.Point;
import org.microcol.gui.background.AbstractAnimatedBackground;
import org.microcol.gui.background.ImageStripePainter;
import org.microcol.gui.background.ImageStripePref;
import org.microcol.gui.background.ThreeStripesPainter;
import org.microcol.gui.background.ThreeStripesPref;
import org.microcol.gui.background.ThreeStripesPref.StripeDef;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class GameMenuBackground extends AbstractAnimatedBackground {

    private final static String IMG_TOP = "sunset-top-line.png";

    private final static String IMG_BOTTOM = "sunset-bottom-line.png";

    private final Image imageTop;

    private final Image imageBottom;

    private final Image imageCenter;

    private final Image bird;

    private final ThreeStripesPainter threeStripesPainter;

    private final ImageStripePainter topImageStripePainter;

    private final ImageStripePainter bottomImageStripePainter;

    private final ScaleLinear moving = new ScaleLinear(-400, 800);

    @Inject
    public GameMenuBackground(final ImageProvider imageProvider) {
        imageTop = Preconditions.checkNotNull(imageProvider.getImage(IMG_TOP));
        imageBottom = Preconditions.checkNotNull(imageProvider.getImage(IMG_BOTTOM));
        imageCenter = Preconditions.checkNotNull(imageProvider.getImage(ImageProvider.IMG_SUNSET));
        bird = Preconditions.checkNotNull(imageProvider.getImage("bird.png"));
        final ThreeStripesPref pref = ThreeStripesPref.build()
                .setTopStripe(StripeDef.of(-240, GuiColors.SKY))
                .setCenterStripe(StripeDef.of(30, GuiColors.OCEAN))
                .setBottomStripe(StripeDef.of(270, GuiColors.GRASS)).setCenterStripeHeight(440)
                .make();
        this.threeStripesPainter = new ThreeStripesPainter(pref);

        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        topImageStripePainter = new ImageStripePainter(ImageStripePref.build().setImage(imageTop)
                .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-258).make());
        bottomImageStripePainter = new ImageStripePainter(
                ImageStripePref.build().setImage(imageBottom)
                        .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(230).make());
    }

    @Override
    public void paint(final GraphicsContext gc) {
        final Point canvasSize = Point.of(getCanvas().getWidth(), getCanvas().getHeight());
        if (canvasSize.getX() > 10000 || canvasSize.getY() > 10000) {
            return;
        }
        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        paintBackground(gc, canvasSize, Color.WHITE);
        threeStripesPainter.paint(gc, canvasSize);
        topImageStripePainter.paint(gc, canvasSize);
        bottomImageStripePainter.paint(gc, canvasSize);
        final Point diff = canvasSize.substract(centerImageSize).divide(2);
        gc.drawImage(imageCenter, diff.getX(), diff.getY());

        // Paint animations, original size 480 × 270
        final Point size = Point.of(480, 270).divide(2).divide(3);
        final Point p = diff.add(moving.getNext(), -20);
        gc.drawImage(bird, p.getX(), p.getY(), size.getX(), size.getY());
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
