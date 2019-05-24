package org.microcol.gui.screen.colony;

import org.microcol.gui.GuiColors;
import org.microcol.gui.Point;
import org.microcol.gui.background.AbstractBackground;
import org.microcol.gui.background.ImageStripePainter;
import org.microcol.gui.background.ImageStripePref;
import org.microcol.gui.background.ThreeStripesPainter;
import org.microcol.gui.background.ThreeStripesPref;
import org.microcol.gui.background.ThreeStripesPref.StripeDef;
import org.microcol.gui.image.ImageProvider;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

@Singleton
class ColonyBackground extends AbstractBackground {

    private final static String IMG_TOP1 = "colony-top1.png";
    private final static String IMG_TOP2 = "colony-top2.png";
    private final static String IMG_BOTTOM = "colony-bottom.png";
    private final static String IMG_CENTER = "colony.png";

    private final Image imageTop1;
    private final Image imageTop2;
    private final Image imageBottom;
    private final Image imageCenter;

    private final ThreeStripesPainter threeStripesPainter;

    private final ImageStripePainter top1ImageStripePainter;
    private final ImageStripePainter top2ImageStripePainter;
    private final ImageStripePainter bottomImageStripePainter;

    @Inject
    public ColonyBackground(final ImageProvider imageProvider) {
        imageTop1 = Preconditions.checkNotNull(imageProvider.getImage(IMG_TOP1));
        imageTop2 = Preconditions.checkNotNull(imageProvider.getImage(IMG_TOP2));
        imageBottom = Preconditions.checkNotNull(imageProvider.getImage(IMG_BOTTOM));
        imageCenter = Preconditions.checkNotNull(imageProvider.getImage(IMG_CENTER));
        final ThreeStripesPref pref = ThreeStripesPref.build()
                .setTopStripe(StripeDef.of(-290, GuiColors.SKY))
                .setCenterStripe(StripeDef.of(80, GuiColors.GRASS))
                .setBottomStripe(StripeDef.of(300, GuiColors.GROUND)).setCenterStripeHeight(420)
                .make();
        this.threeStripesPainter = new ThreeStripesPainter(pref);

        final Point centerImageSize = Point.of(imageCenter.getWidth(), imageCenter.getHeight());
        top1ImageStripePainter = new ImageStripePainter(ImageStripePref.build().setImage(imageTop1)
                .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-305).make());
        top2ImageStripePainter = new ImageStripePainter(ImageStripePref.build().setImage(imageTop2)
                .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(-298).make());
        bottomImageStripePainter = new ImageStripePainter(
                ImageStripePref.build().setImage(imageBottom)
                        .setCenterGap(centerImageSize.getX() - 10).setVerticalShift(265).make());
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
        top1ImageStripePainter.paintLeft(gc, canvasSize);
        top2ImageStripePainter.paintRight(gc, canvasSize);
        bottomImageStripePainter.paint(gc, canvasSize);
        final Point diff = canvasSize.substract(centerImageSize).divide(2);
        gc.drawImage(imageCenter, diff.getX(), diff.getY());
    }

}
